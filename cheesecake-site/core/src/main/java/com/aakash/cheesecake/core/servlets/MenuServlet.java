package com.aakash.cheesecake.core.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

@Component(service = Servlet.class)
@SlingServletPaths("/graphql/execute.json/cheesecake/menu")
public class MenuServlet extends SlingSafeMethodsServlet {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    protected void doGet(SlingHttpServletRequest request,
                         SlingHttpServletResponse response)
                         throws ServletException, IOException {

        // Fix 1 — null check on getResource()
        Resource menuResource = request.getResourceResolver()
                .getResource("/content/cheesecake/menu");

        if (menuResource == null) {
            response.sendError(SlingHttpServletResponse.SC_NOT_FOUND,
                    "Menu content not found");
            return;
        }

        // Fix 3 — Jackson builds JSON safely
        ObjectNode root = MAPPER.createObjectNode();
        ObjectNode data = root.putObject("data");
        ArrayNode menuItems = data.putArray("menuItems");

        for (Resource item : menuResource.getChildren()) {

            // Fix 2 — ResourceUtil.getValueMap() never returns null
            ValueMap props = ResourceUtil.getValueMap(item);

            ObjectNode menuItem = menuItems.addObject();
            menuItem.put("title",       props.get("jcr:title",    ""));
            menuItem.put("price",       props.get("price",         ""));
            menuItem.put("description", props.get("description",   ""));
            menuItem.put("available",   props.get("available",     false));
        }

        response.setHeader("Cache-Control", "max-age=300, public");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        MAPPER.writeValue(response.getWriter(), root);
    }
}