package org.example;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.directive.Include;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.exception.MethodInvocationException;

import java.io.IOException;
import java.io.Writer;

public class MyCustomIncludeDirective extends Include {
//    @Override
//    public String getName() {
//        return "include"; // Keep the same name to override the existing include directive
//    }

    // Define the name of the directive, which will be used in templates.
//    @Override
//    public String getName() {
//        return "include";
//    }

    // Define the type of directive: BLOCK or LINE.
//    @Override
//    public int getType() {
//        return Directive.LINE; // or LINE if it's a single-line directive
//    }

    // Implement the rendering logic for the directive.
//    @Override
//    public boolean render(InternalContextAdapter context, Writer writer, Node node)
//            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
//
//        // Example: Get the first argument passed to the directive
//        String param = (String) node.jjtGetChild(0).value(context);
//
//        // Perform some logic with the parameter
//        writer.write("Hello, " + param + "! This is a custom directive.");
//
//        return true; // Indicate the directive was processed successfully
//    }

//    public boolean render(InternalContextAdapter context, Writer writer) throws IOException {
//        try {
//            writer.write("Custom render logic for directive: " + "directiveName" + "\n");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        writer.write("Hello, " + "param" + "! This is a custom directive.");
//        super.render(context, writer);
//
//        // Optionally, call the superclass's render method to include original behavior
//        // return super.render(context, writer);
//
//        // If overriding entirely, return true or false based on your custom logic
//        return true;
//    }

    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node)
            throws IOException, MethodInvocationException {

        // Custom logic before the include processing
        writer.write("<!-- Start of custom include logic -->\n");

        // Call the superclass render method to execute the default include behavior

        boolean result = super.render(context, writer, node);

        // Custom logic after the include processing
        writer.write("\n<!-- End of custom include logic -->\n");

        return result;
    }

}

