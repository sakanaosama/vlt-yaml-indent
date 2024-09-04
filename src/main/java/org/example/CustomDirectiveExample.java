package org.example;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.StringWriter;
import java.util.Properties;

public class CustomDirectiveExample {

    public static void main(String[] args){

        // Initialize Velocity engine
        VelocityEngine velocityEngine = new VelocityEngine();
        Properties props = new Properties();
        props.setProperty("classpath.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

        props.setProperty("file.resource.loader.path", "src/main/resources"); // Template path
//        props.setProperty("space.gobbling", "structured");
//        props.setProperty("space.gobbling", "none");
//        props.setProperty("space.gobbling", "lines");
//        props.setProperty("space.gobbling", "bc");

        props.setProperty("userdirective", "org.example.MyCustomDirective");
//        props.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
//        props.setProperty("runtime.log", "C:\\workspace\\nx-workspace\\example\\vlt-yaml-indent\\vlt-yaml-indent\\src\\main\\resources\\logs\\velocity.log");



        velocityEngine.init(props);

        // Get the main template
        Template template = velocityEngine.getTemplate("config.vm");
//        Template template = velocityEngine.getTemplate("include_main_template.vm");
//        Template template = velocityEngine.getTemplate("configuration.yaml");

        // Create the context and add data
        VelocityContext context = new VelocityContext();
//        context.put("title", "Sample Document");
//        context.put("date", "2024-08-20");
//        context.put("items", "blahblahblah");
//        context.put("key2b2Content", "blahblah");

        // Merge template with context data
        StringWriter writer = new StringWriter();




        velocityEngine.mergeTemplate("config.vm", "UTF-8", context, writer);

        // Output the processed YAML content
        System.out.println(writer.toString());





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
