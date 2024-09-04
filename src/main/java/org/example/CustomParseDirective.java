package org.example;

import org.apache.velocity.Template;
import org.apache.velocity.app.event.EventHandlerUtil;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.*;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.directive.MacroParseException;
import org.apache.velocity.runtime.directive.Parse;
import org.apache.velocity.runtime.directive.StopCommand;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.parser.node.*;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.util.StringUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class CustomParseDirective extends Parse
{
    private int maxDepth;
    public boolean strictRef = false;

    @Override
    public void init(RuntimeServices rs, InternalContextAdapter context, Node node)
            throws TemplateInitException
    {
        super.init(rs, context, node);

        this.maxDepth = rsvc.getInt(RuntimeConstants.PARSE_DIRECTIVE_MAXDEPTH, 10);

        strictRef = rsvc.getBoolean(RuntimeConstants.RUNTIME_REFERENCES_STRICT, false);
    }

    @Override
    public boolean render(InternalContextAdapter context,
                          Writer writer, Node node)
            throws IOException, ResourceNotFoundException, ParseErrorException,
            MethodInvocationException
    {
        /*
         *  did we get an argument?
         */
        if ( node.jjtGetNumChildren() == 0 )
        {
            throw new VelocityException("#parse(): argument missing at " +
                    StringUtils.formatFileString(this), null, rsvc.getLogContext().getStackTrace());
        }

        /*
         *  does it have a value?  If you have a null reference, then no.
         */
        Object value =  node.jjtGetChild(0).value( context );
        if (value == null)
        {
            log.debug("#parse(): null argument at {}", StringUtils.formatFileString(this));
        }

        /*
         *  get the path
         */
        String sourcearg = value == null ? null : value.toString();

        /*
         *  check to see if the argument will be changed by the event cartridge
         */
        String arg = EventHandlerUtil.includeEvent( rsvc, context, sourcearg, context.getCurrentTemplateName(), getName());

        /*
         * if strict mode and arg was not fixed by event handler, then complain
         */
        if (strictRef && value == null && arg == null)
        {
            throw new VelocityException("The argument to #parse returned null at "
                    + StringUtils.formatFileString(this), null, rsvc.getLogContext().getStackTrace());
        }

        /*
         *   a null return value from the event cartridge indicates we should not
         *   input a resource.
         */
        if (arg == null)
        {
            // abort early, but still consider it a successful rendering
            return true;
        }


        if (maxDepth > 0)
        {
            /*
             * see if we have exceeded the configured depth.
             */
            String[] templateStack = context.getTemplateNameStack();
            if (templateStack.length >= maxDepth)
            {
                StringBuilder path = new StringBuilder();
                for (String aTemplateStack : templateStack)
                {
                    path.append(" > ").append(aTemplateStack);
                }
                log.error("Max recursion depth reached ({}). File stack: {}",
                        templateStack.length, path);

                return false;
            }
        }

        /*
         *  now use the Runtime resource loader to get the template
         */

        Template t = null;

        try
        {
            t = rsvc.getTemplate( arg, getInputEncoding(context) );
        }
        catch ( ResourceNotFoundException rnfe )
        {
            /*
             * the arg wasn't found.  Note it and throw
             */
            log.error("#parse(): cannot find template '{}', called at {}",
                    arg, StringUtils.formatFileString(this));
            throw rnfe;
        }
        catch ( ParseErrorException pee )
        {
            /*
             * the arg was found, but didn't parse - syntax error
             *  note it and throw
             */
            log.error("#parse(): syntax error in #parse()-ed template '{}', called at {}",
                    arg, StringUtils.formatFileString(this));
            throw pee;
        }
        /*
         * pass through application level runtime exceptions
         */
        catch( RuntimeException e )
        {
            log.error("Exception rendering #parse({}) at {}",
                    arg, StringUtils.formatFileString(this));
            throw e;
        }
        catch ( Exception e )
        {
            String msg = "Exception rendering #parse(" + arg + ") at " +
                    StringUtils.formatFileString(this);
            log.error(msg, e);
            throw new VelocityException(msg, e, rsvc.getLogContext().getStackTrace());
        }

        /*
         * Add the template name to the macro libraries list
         */
        List<Template> macroLibraries = context.getMacroLibraries();

        /*
         * if macroLibraries are not set create a new one
         */
        if (macroLibraries == null)
        {
            macroLibraries = new ArrayList<>();
        }

        context.setMacroLibraries(macroLibraries);

        /* instead of adding the name of the template, add the Template reference */
        macroLibraries.add(t);

        /*
         *  and render it
         */
        try
        {
            preRender(context);
            context.pushCurrentTemplateName(arg);


            //Start POC--------------------------------------

            String prefixSpace=((ASTDirective)node).getPrefix();
            System.out.println("---" + ((ASTDirective)node).getDirectiveName() + ":"
                    + ((ASTDirective)node).getTemplateName() +
                    "------------ prefix Space '" + prefixSpace.length() + "' "
            );


            Field childrenField = SimpleNode.class.getDeclaredField("children");
            childrenField.setAccessible(true); // Make the protected childrenField accessible

            // Get the value of 'firstImage' from the SimpleNode instance
            Node[] childrenNodes = (Node[]) childrenField.get(t.getData());

            boolean afterNonASTText = false;

            for (int index = 0; index < childrenNodes.length; index++) {
                Node childrenNode = childrenNodes[index];
                if (childrenNode instanceof ASTText) {
                    ASTText chNode = (ASTText) childrenNode;
                    String currentText = chNode.getCtext();

                    if (index == 0 || afterNonASTText) {
                        currentText = String.valueOf("^").repeat(prefixSpace.length()) + currentText;
//                        currentText = prefixSpace + currentText;
                    }

                    // Add "space" to suffix for all except the last chNode
//                    if (index < childrenNodes.length - 1) {
//                        //todo
//                        // node should end with newln or directive?
//                        currentText = currentText + String.valueOf("*").repeat(prefixSpace.length());
////                        currentText = currentText + prefixSpace + "*";
//                    }

                    // Check for newline character and set the modified text
                    if (currentText.contains("\r\n")) {
                        int lastIndex = currentText.lastIndexOf("\r\n");

                        // Replace all "\r\n" with "\r\n" + prefixSpace except the last one
                        String temp = String.valueOf("@").repeat(prefixSpace.length());
                        String beforeLast = currentText.substring(0, lastIndex).replace("\r\n", "\r\n" + temp );
//                        String beforeLast = currentText.substring(0, lastIndex).replace("\r\n", "\r\n" + prefixSpace + "#");
                        String afterLast = currentText.substring(lastIndex);

                        // Combine the modified text and set it to chNode
                        currentText = beforeLast + afterLast;
                        chNode.setCtext(currentText);
                    }

                }else if(childrenNode instanceof ASTDirective){

                    afterNonASTText = true;
                    System.out.println("---instanceof ASTDirective: " + ((ASTDirective)childrenNode).getDirectiveName() + ":"
                            + ((ASTDirective)childrenNode).getTemplateName()


                    );

                    //todo
                    // Add prefixSpace to the Node
                    ((ASTDirective)childrenNode).setPrefix(String.valueOf("%").repeat(prefixSpace.length()) + ((ASTDirective)childrenNode).getPrefix());


                }else {
//                    afterNonASTText = true;
                }
            }

//            ASTText chNode = (ASTText)childrenNodes[0];
//            chNode.setCtext("xxxxx" + chNode.getCtext());

            Writer temp = new StringWriter();

            ((SimpleNode) t.getData()).render(context, temp);

            System.out.println("!print!---------------------------:");
            System.out.print(temp.toString());
            System.out.println("!print!---------------------------:");



            writer.append(temp.toString());

//            System.out.println("num of childrenNodes" + childrenNodes.length);
        }
        catch( StopCommand stop )
        {
            if (!stop.isFor(this))
            {
                throw stop;
            }
        }
        /*
         * pass through application level runtime exceptions
         */
        catch( RuntimeException e )
        {
            /*
             * Log #parse errors so the user can track which file called which.
             */
            log.error("Exception rendering #parse({}) at {}",
                    arg, StringUtils.formatFileString(this));
            throw e;
        }
        catch ( Exception e )
        {
            String msg = "Exception rendering #parse(" + arg + ") at " +
                    StringUtils.formatFileString(this);
            log.error(msg, e);
            throw new VelocityException(msg, e, rsvc.getLogContext().getStackTrace());
        }
        finally
        {
            context.popCurrentTemplateName();
            postRender(context);
        }

        /*
         *    note - a blocked input is still a successful operation as this is
         *    expected behavior.
         */

        return true;
    }

    /**
     * Called by the parser to validate the argument types
     */
//    @Override
//    public void checkArgs(ArrayList<Integer> argtypes, Token t, String templateName)
//            throws ParseException
//    {
//        if (argtypes.size() != 1)
//        {
//            throw new MacroParseException("The #parse directive requires one argument",
//                    templateName, t);
//        }
//
//        if (argtypes.get(0) == ParserTreeConstants.JJTWORD)
//        {
//            throw new MacroParseException("The argument to #parse is of the wrong type",
//                    templateName, t);
//        }
//    }
}


