package org.example;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.runtime.parser.node.ASTDirective;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.context.InternalContextAdapter;

import java.io.Writer;
import java.io.IOException;

public class CustomASTDirective extends ASTDirective {
    public CustomASTDirective(int id) {
        super(id);
    }

//    @Override
//    public String getName() {
//        return "custom"; // The name of the directive, used in the template as #custom
//    }

    @Override
    public int getType() {
        return Directive.LINE; // Type of directive: LINE or BLOCK
    }

    @Override
    public boolean render(InternalContextAdapter context, Writer writer) throws IOException {
        try {
            writer.write("Custom render logic for directive: " + "directiveName" + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.render(context, writer);

        // Optionally, call the superclass's render method to include original behavior
        // return super.render(context, writer);

        // If overriding entirely, return true or false based on your custom logic
        return true;
    }
}
