package org.example;

import org.apache.velocity.app.event.EventHandlerUtil;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.directive.Include;
import org.apache.velocity.runtime.parser.node.*;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.util.StringUtils;

import java.io.IOException;
import java.io.Writer;

public class MyCustomIncludeDirective extends Include
{
        private String outputMsgStart = "";
        private String outputMsgEnd = "";

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

            String indentedData = ((String)resource.getData()).replaceAll("(?m)^", node.jjtGetParent().getFirstTokenImage());

            writer.write(indentedData);

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


