/*
MIT License

Copyright (c) 2020 Ming Zhou

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package info.julang.ide.launcher.console;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.IShowInTargetList;

/**
 * Provide a termination action to Julian Console.
 * 
 * @author Ming Zhou
 */
public class JulianConsolePageParticipant implements IConsolePageParticipant, IShowInTargetList {
	
	// IMPLEMENTATION NOTES
	// Inspire by JDT's debug console:
	// https://github.com/eclipse/eclipse.platform.debug/blob/master/org.eclipse.debug.ui/ui/org/eclipse/debug/internal/ui/views/console/ProcessConsolePageParticipant.java
	
	private static final String s_contextId = "org.eclipse.debug.ui.console";

	private TerminateAction action;
    private JulianConsole jconsole;
    private IPageBookViewPage viewPage;
    private IContextActivation cntxActivation;
    private IHandlerActivation hdlrActivation;

    @Override
	public void init(IPageBookViewPage page, IConsole console) {
        viewPage = page;
        jconsole = (JulianConsole) console;

        action = new TerminateAction(page.getSite().getWorkbenchWindow(), jconsole);

        IActionBars actionBars = viewPage.getSite().getActionBars();
        configureToolBar(actionBars.getToolBarManager());
    }

    @Override
	public void dispose() {
	    action = null;
		jconsole = null;
    }

    protected void configureToolBar(IToolBarManager mgr) {
		mgr.appendToGroup(IConsoleConstants.LAUNCH_GROUP, action);
    }

	@Override
	public <T> T getAdapter(Class<T> required) {
        return null;
    }

    @Override
	public String[] getShowInTargetIds() {
        return new String[] {IDebugUIConstants.ID_DEBUG_VIEW};
    }

    @Override
	public void activated() {
        IPageSite site = viewPage.getSite();
        if(cntxActivation == null && hdlrActivation == null) {
	        IContextService contextService = site.getService(IContextService.class);
	        cntxActivation = contextService.activateContext(s_contextId);
        }
    }
    
    @Override
	public void deactivated() {
        IPageSite site = viewPage.getSite();
        IContextService contextService = site.getService(IContextService.class);
		contextService.deactivateContext(cntxActivation);
		cntxActivation = null;
    }
}