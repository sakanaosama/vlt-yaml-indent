package org.example;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.MethodInvocationException;

import java.io.StringWriter;
import java.util.Properties;

public class VelocityEvaluateExample {
    public static void main(String[] args) {
        // Initialize the Velocity Engine
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.init();

        // Create a context and add data
        VelocityContext context = new VelocityContext();
        context.put("name", "John");
        context.put("greeting", "Hello");

        // Define the template string with a syntax error (missing closing brace for the foreach loop)
        String template = "$greeting, $name! Welcome to Velocity.\n#foreach($item in $items\n$item\n #end\n";

        // Writer to hold the output
        StringWriter writer = new StringWriter();

        // Evaluate the template against the context
        try {
            velocityEngine.evaluate(context, writer, "logTag", template);
        } catch (ParseErrorException e) {
            System.err.println("Template parsing error: " + e.getMessage());
        } catch (MethodInvocationException e) {
            System.err.println("Method invocation error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("General error: " + e.getMessage());
        }

        // Output the result (will not be reached if there's an exception)
        System.out.println(writer.toString());
    }
}
