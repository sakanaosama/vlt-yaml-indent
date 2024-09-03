package org.example;

import org.apache.velocity.app.event.EventHandlerUtil;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.directive.Include;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.runtime.parser.node.ParserTreeConstants;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.util.StringUtils;

import java.io.IOException;
import java.io.Writer;

public class MyCustomIncludeDirective extends Include implements MyCustomIncludeDirectiveExInterface
{
        private String outputMsgStart = "";
        private String outputMsgEnd = "";

        /**
         * Return name of this directive.
         * @return The name of this directive.
         */
        @Override
        public String getName()
        {
            return "include";
        }

        /**
         * Return type of this directive.
         * @return The type of this directive.
         */
        @Override
        public int getType()
        {
            return LINE;
        }

        /**
         * Since there is no processing of content,
         * there is never a need for an internal scope.
         */
        @Override
        public boolean isScopeProvided()
        {
            return false;
        }

        /**
         *  simple init - init the tree and get the elementKey from
         *  the AST
         * @param rs
         * @param context
         * @param node
         * @throws TemplateInitException
         */
        @Override
        public void init(RuntimeServices rs, InternalContextAdapter context,
                         Node node)
                throws TemplateInitException
        {
            super.init( rs, context, node );

            /*
             *  get the msg, and add the space so we don't have to
             *  do it each time
             */
            outputMsgStart = rsvc.getString(RuntimeConstants.ERRORMSG_START);
            outputMsgStart = outputMsgStart + " ";

            outputMsgEnd = rsvc.getString(RuntimeConstants.ERRORMSG_END );
            outputMsgEnd = " " + outputMsgEnd;
        }

        /**
         *  iterates through the argument list and renders every
         *  argument that is appropriate.  Any non appropriate
         *  arguments are logged, but render() continues.
         * @param context
         * @param writer
         * @param node
         * @return True if the directive rendered successfully.
         * @throws IOException
         * @throws MethodInvocationException
         * @throws ResourceNotFoundException
         */
        @Override
        public boolean render(InternalContextAdapter context,
                              Writer writer, Node node)
                throws IOException, MethodInvocationException,
                ResourceNotFoundException
        {
            /*
             *  get our arguments and check them
             */

            int argCount = node.jjtGetNumChildren();

            for( int i = 0; i < argCount; i++)
            {
                /*
                 *  we only handle StringLiterals and References right now
                 */

                Node n = node.jjtGetChild(i);

                if ( n.getType() ==  ParserTreeConstants.JJTSTRINGLITERAL ||
                        n.getType() ==  ParserTreeConstants.JJTREFERENCE )
                {
                    if (!renderOutput( n, context, writer ))
                        outputErrorToStream( writer, "error with arg " + i
                                + " please see log.");
                }
                else
                {
                    String msg = "invalid #include() argument '"
                            + n.toString() + "' at " + StringUtils.formatFileString(this);
                    log.error(msg);
                    outputErrorToStream( writer, "error with arg " + i
                            + " please see log.");
                    throw new VelocityException(msg, null, rsvc.getLogContext().getStackTrace());
                }
            }

            return true;
        }

        /**
         *  does the actual rendering of the included file
         *
         *  @param node AST argument of type StringLiteral or Reference
         *  @param context valid context so we can render References
         *  @param writer output Writer
         *  @return boolean success or failure.  failures are logged
         *  @exception IOException
         *  @exception MethodInvocationException
         *  @exception ResourceNotFoundException
         */
        private boolean renderOutput( Node node, InternalContextAdapter context,
                                      Writer writer )
                throws IOException, MethodInvocationException,
                ResourceNotFoundException
        {
            if ( node == null )
            {
                log.error("#include() null argument");
                return false;
            }

            /*
             *  does it have a value?  If you have a null reference, then no.
             */
            Object value = node.value( context );
            if ( value == null)
            {
                log.error("#include() null argument");
                return false;
            }

            /*
             *  get the path
             */
            String sourcearg = value.toString();

            /*
             *  check to see if the argument will be changed by the event handler
             */

            String arg = EventHandlerUtil.includeEvent( rsvc, context, sourcearg, context.getCurrentTemplateName(), getName() );

            /*
             *   a null return value from the event cartridge indicates we should not
             *   input a resource.
             */
            boolean blockinput = false;
            if (arg == null)
                blockinput = true;

            Resource resource = null;

            try
            {
                if (!blockinput)
                    resource = rsvc.getContent(arg, getInputEncoding(context));
            }
            catch ( ResourceNotFoundException rnfe )
            {
                /*
                 * the arg wasn't found.  Note it and throw
                 */
                log.error("#include(): cannot find resource '{}', called at {}",
                        arg, StringUtils.formatFileString(this));
                throw rnfe;
            }

            /*
             * pass through application level runtime exceptions
             */
            catch( RuntimeException e )
            {
                log.error("#include(): arg = '{}', called at {}",
                        arg, StringUtils.formatFileString(this));
                throw e;
            }
            catch (Exception e)
            {
                String msg = "#include(): arg = '" + arg +
                        "', called at " + StringUtils.formatFileString(this);
                log.error(msg, e);
                throw new VelocityException(msg, e, rsvc.getLogContext().getStackTrace());
            }


            /*
             *    note - a blocked input is still a successful operation as this is
             *    expected behavior.
             */

            if ( blockinput )
                return true;

            else if ( resource == null )
                return false;

            String indentedData = ((String)resource.getData()).replaceAll("(?m)^", "    ");
            writer.write(indentedData);
//            writer.write((String)resource.getData());

            return true;
        }

        /**
         *  Puts a message to the render output stream if ERRORMSG_START / END
         *  are valid property strings.  Mainly used for end-user template
         *  debugging.
         *  @param writer
         *  @param msg
         *  @throws IOException
         *  @deprecated if/how errors are displayed is not the concern of the engine, which should throw in all cases
         */
        private void outputErrorToStream( Writer writer, String msg )
                throws IOException
        {
            if ( outputMsgStart != null  && outputMsgEnd != null)
            {
                writer.write(outputMsgStart);
                writer.write(msg);
                writer.write(outputMsgEnd);
            }
        }
}

//{
////    @Override
////    public String getName() {
////        return "include"; // Keep the same name to override the existing include directive
////    }
//
//    // Define the name of the directive, which will be used in templates.
////    @Override
////    public String getName() {
////        return "include";
////    }
//
//    // Define the type of directive: BLOCK or LINE.
////    @Override
////    public int getType() {
////        return Directive.LINE; // or LINE if it's a single-line directive
////    }
//
//    // Implement the rendering logic for the directive.
////    @Override
////    public boolean render(InternalContextAdapter context, Writer writer, Node node)
////            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
////
////        // Example: Get the first argument passed to the directive
////        String param = (String) node.jjtGetChild(0).value(context);
////
////        // Perform some logic with the parameter
////        writer.write("Hello, " + param + "! This is a custom directive.");
////
////        return true; // Indicate the directive was processed successfully
////    }
//
////    public boolean render(InternalContextAdapter context, Writer writer) throws IOException {
////        try {
////            writer.write("Custom render logic for directive: " + "directiveName" + "\n");
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
////
////        writer.write("Hello, " + "param" + "! This is a custom directive.");
////        super.render(context, writer);
////
////        // Optionally, call the superclass's render method to include original behavior
////        // return super.render(context, writer);
////
////        // If overriding entirely, return true or false based on your custom logic
////        return true;
////    }
//
//    @Override
//    public boolean render(InternalContextAdapter context,
//                          Writer writer, Node node)
//            throws IOException, MethodInvocationException,
//            ResourceNotFoundException
//    {
//        /*
//         *  get our arguments and check them
//         */
//
//        int argCount = node.jjtGetNumChildren();
//
//        for( int i = 0; i < argCount; i++)
//        {
//            /*
//             *  we only handle StringLiterals and References right now
//             */
//
//            Node n = node.jjtGetChild(i);
//
//            if ( n.getType() ==  ParserTreeConstants.JJTSTRINGLITERAL ||
//                    n.getType() ==  ParserTreeConstants.JJTREFERENCE )
//            {
//                if (!modRenderOutput( n, context, writer ))
//                    modOutputErrorToStream( writer, "error with arg " + i
//                            + " please see log.");
//            }
//            else
//            {
//                String msg = "invalid #include() argument '"
//                        + n.toString() + "' at " + StringUtils.formatFileString(this);
//                log.error(msg);
//                modOoutputErrorToStream( writer, "error with arg " + i
//                        + " please see log.");
//                throw new VelocityException(msg, null, rsvc.getLogContext().getStackTrace());
//            }
//        }
//
//        return true;
//    }
//
//
//
//
////    public boolean render(InternalContextAdapter context, Writer writer, Node node)
////            throws IOException, MethodInvocationException {
////
////        // Custom logic before the include processing
////        writer.write("<!-- Start of custom include logic -->\n");
////
////        // Call the superclass render method to execute the default include behavior
////
////        boolean result = super.render(context, writer, node);
////
////        // Custom logic after the include processing
////        writer.write("\n<!-- End of custom include logic -->\n");
////
////        return result;
////    }
//
//
//    private boolean modRenderOutput( Node node, InternalContextAdapter context,
//                                  Writer writer )
//            throws IOException, MethodInvocationException,
//            ResourceNotFoundException
//    {
//        if ( node == null )
//        {
//            log.error("#include() null argument");
//            return false;
//        }
//
//        /*
//         *  does it have a value?  If you have a null reference, then no.
//         */
//        Object value = node.value( context );
//        if ( value == null)
//        {
//            log.error("#include() null argument");
//            return false;
//        }
//
//        /*
//         *  get the path
//         */
//        String sourcearg = value.toString();
//
//        /*
//         *  check to see if the argument will be changed by the event handler
//         */
//
//        String arg = EventHandlerUtil.includeEvent( rsvc, context, sourcearg, context.getCurrentTemplateName(), getName() );
//
//        /*
//         *   a null return value from the event cartridge indicates we should not
//         *   input a resource.
//         */
//        boolean blockinput = false;
//        if (arg == null)
//            blockinput = true;
//
//        Resource resource = null;
//
//        try
//        {
//            if (!blockinput)
//                resource = rsvc.getContent(arg, getInputEncoding(context));
//        }
//        catch ( ResourceNotFoundException rnfe )
//        {
//            /*
//             * the arg wasn't found.  Note it and throw
//             */
//            log.error("#include(): cannot find resource '{}', called at {}",
//                    arg, StringUtils.formatFileString(this));
//            throw rnfe;
//        }
//
//        /*
//         * pass through application level runtime exceptions
//         */
//        catch( RuntimeException e )
//        {
//            log.error("#include(): arg = '{}', called at {}",
//                    arg, StringUtils.formatFileString(this));
//            throw e;
//        }
//        catch (Exception e)
//        {
//            String msg = "#include(): arg = '" + arg +
//                    "', called at " + StringUtils.formatFileString(this);
//            log.error(msg, e);
//            throw new VelocityException(msg, e, rsvc.getLogContext().getStackTrace());
//        }
//
//
//        /*
//         *    note - a blocked input is still a successful operation as this is
//         *    expected behavior.
//         */
//
//        if ( blockinput )
//            return true;
//
//        else if ( resource == null )
//            return false;
//
//        writer.write((String)resource.getData());
//        return true;
//    }
//
//    private void outputErrorToStream( Writer writer, String msg )
//            throws IOException
//    {
//        if ( outputMsgStart != null  && outputMsgEnd != null)
//        {
//            writer.write(outputMsgStart);
//            writer.write(msg);
//            writer.write(outputMsgEnd);
//        }
//    }
//
//}

