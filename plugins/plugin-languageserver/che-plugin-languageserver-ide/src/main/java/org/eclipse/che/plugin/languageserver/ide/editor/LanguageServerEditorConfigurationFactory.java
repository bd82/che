/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.languageserver.ide.editor;

import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;


/**
 * @author Evgen Vidolob
 */
public interface LanguageServerEditorConfigurationFactory {

    LanguageServerEditorConfiguration build(TextEditor editor, ServerCapabilities capabilities);
}
