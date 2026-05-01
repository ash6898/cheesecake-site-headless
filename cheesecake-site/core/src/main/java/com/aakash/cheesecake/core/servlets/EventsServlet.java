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
@SlingServletPaths("/graphql/execute.json/cheesecake/events")
public class EventsServlet extends SlingSafeMethodsServlet {
    
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    protected void doGet(SlingHttpServletRequest request,
                        SlingHttpServletResponse response)
                        throws ServletException, IOException {
                        
        Resource eventsResource = request.getResourceResolver()
                .getResource("/content/cheesecake/events");

        if (eventsResource == null) {
            response.sendError(SlingHttpServletResponse.SC_NOT_FOUND,
                    "Events content not found");
            return;
        }

        ObjectNode root = MAPPER.createObjectNode();
        ObjectNode data = root.putObject("data");
        ArrayNode events = data.putArray("events");

        for (Resource event : eventsResource.getChildren()) {

            ValueMap props = ResourceUtil.getValueMap(event);

            ObjectNode eventNode = events.addObject();
            eventNode.put("title", props.get("jcr:title", ""));
            eventNode.put("location", props.get("location", ""));
            eventNode.put("date", props.get("date", ""));
            eventNode.put("hours",  props.get("hours", ""));
            eventNode.put("active", props.get("active", false));
        }
        response.setHeader("Cache-Control", "max-age=300, public");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        MAPPER.writeValue(response.getWriter(), root);
    }
}
