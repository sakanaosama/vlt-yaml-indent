package org.example;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Main {

    public static void main(String[] args){

        // Initialize Velocity Engine
        Properties properties = new Properties();
        properties.setProperty("file.resource.loader.path", "src/main/resources/");

//                properties.setProperty("space.gobbling", "structured");
//        properties.setProperty("space.gobbling", "none");
//        properties.setProperty("space.gobbling", "lines");

        VelocityEngine velocityEngine = new VelocityEngine(properties);
        velocityEngine.init();

        // Preprocess the included file to add indentation
        StringBuilder indentedContent = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/key2b2.yaml"))) {
            String line;
            while ((line = br.readLine()) != null) {
                // TODO: 2024-08-23
                //  How do I know the leg of the indents?????
                indentedContent.append("----").append(line).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(indentedContent);


        // Prepare the context and add the indented content
        VelocityContext context = new VelocityContext();
        context.put("key2b2Content", indentedContent.toString());

        // Merge the template with the context
        StringWriter writer = new StringWriter();
        velocityEngine.mergeTemplate("config.yaml", "UTF-8", context, writer);

        // Output the processed YAML content
        System.out.println(writer.toString());

//
//        // Initialize Velocity engine
//        VelocityEngine velocityEngine = new VelocityEngine();
//        Properties props = new Properties();
//        props.setProperty("file.resource.loader.path", "src/main/resources"); // Template path
////        props.setProperty("space.gobbling", "structured");
////        props.setProperty("space.gobbling", "none");
////        props.setProperty("space.gobbling", "lines");
//
//
//        velocityEngine.init(props);
//
//        // Get the main template
//        Template template = velocityEngine.getTemplate("config.yaml");
////        Template template = velocityEngine.getTemplate("main_template.vm");
////        Template template = velocityEngine.getTemplate("configuration.yaml");
//
//        // Create the context and add data
//        VelocityContext context = new VelocityContext();
//        context.put("title", "Sample Document");
//        context.put("date", "2024-08-20");
//
//        // Create a list of items
//        List<Map<String, String>> items = new ArrayList<>();
//        Map<String, String> item1 = new HashMap<>();
//        item1.put("name", "Item1");
//        item1.put("value", "Value1");
//
//        Map<String, String> item2 = new HashMap<>();
//        item2.put("name", "Item2");
//        item2.put("value", "Value2");
//
//        items.add(item1);
//        items.add(item2);
//
//        context.put("items", items);
//
//        // Merge template with context data
//        StringWriter writer = new StringWriter();
//        template.merge(context, writer);
//
//        // Output the XML
//        System.out.println(writer.toString());







//
//
//
//        // Create the context and add data
//        VelocityContext context = new VelocityContext();
//
//        // Merge template with context data
//        StringWriter writer = new StringWriter();
//        template.merge(context, writer);
//
//        // Output the YAML
//        System.out.println(writer.toString());
//
//        // Add data to the context
//        context.put("key1", "value1");
//        context.put("key2", "value2");
//
//        // Merge the template with the updated context and output the result to a new StringWriter
//        StringWriter updatedWriter = new StringWriter();
//        template.merge(context, updatedWriter);
//
//        // Print the template to the screen again (with added context)
//        System.out.println("\nTemplate with updated context:");
//        System.out.println(updatedWriter.toString());

    }
}
