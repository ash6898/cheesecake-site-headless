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
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * JSON for the menu collection.
 *
 * Bound by resource type, selector, and extension (Sling / AEM practice),
 * not by a fixed path. Sling invokes this servlet for:
 * GET /content/cheesecake/menu.model.json
 * when that resource has sling:resourceType=cheesecake/components/menu.
 * The body is hand-built JSON. It is not a GraphQL result and not the
 * Sling Model Exporter, even though the selector matches the .model.json shape.
 */
@Component(service = Servlet.class)
@SlingServletResourceTypes(
        resourceTypes = "cheesecake/components/menu",
        selectors = "model",
        extensions = "json",
        methods = "GET"
)
public class MenuServlet extends SlingSafeMethodsServlet {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    protected void doGet(SlingHttpServletRequest request,
                         SlingHttpServletResponse response)
                         throws ServletException, IOException {

        Resource menuResource = request.getResource();
        if (ResourceUtil.isNonExistingResource(menuResource)) {
            response.sendError(SlingHttpServletResponse.SC_NOT_FOUND,
                    "Menu content not found");
            return;
        }

        ObjectNode root = MAPPER.createObjectNode();
        ObjectNode data = root.putObject("data");
        ArrayNode menuItems = data.putArray("menuItems");

        for (Resource item : menuResource.getChildren()) {
            if (isSystemNode(item)) {
                continue;
            }

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

    private static boolean isSystemNode(Resource item) {
        String name = item.getName();
        return name.startsWith("jcr:") || name.startsWith("rep:");
    }
}
