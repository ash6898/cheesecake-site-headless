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
 * JSON for the events collection.
 *
 * Bound by resource type, selector, and extension. Sling invokes this
 * servlet for GET /content/cheesecake/events.model.json when that resource
 * has sling:resourceType=cheesecake/components/events.
 * The body is hand-built JSON, not a GraphQL execution.
 */
@Component(service = Servlet.class)
@SlingServletResourceTypes(
        resourceTypes = "cheesecake/components/events",
        selectors = "model",
        extensions = "json",
        methods = "GET"
)
public class EventsServlet extends SlingSafeMethodsServlet {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    protected void doGet(SlingHttpServletRequest request,
                        SlingHttpServletResponse response)
                        throws ServletException, IOException {

        Resource eventsResource = request.getResource();
        if (ResourceUtil.isNonExistingResource(eventsResource)) {
            response.sendError(SlingHttpServletResponse.SC_NOT_FOUND,
                    "Events content not found");
            return;
        }

        ObjectNode root = MAPPER.createObjectNode();
        ObjectNode data = root.putObject("data");
        ArrayNode events = data.putArray("events");

        for (Resource event : eventsResource.getChildren()) {
            if (isSystemNode(event)) {
                continue;
            }

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

    private static boolean isSystemNode(Resource item) {
        String name = item.getName();
        return name.startsWith("jcr:") || name.startsWith("rep:");
    }
}
