/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.glassfish.admin.rest.resources;

import com.sun.enterprise.config.serverbeans.Domain;
import javax.ws.rs.core.Context;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.Dom;

/**
 * This is the root class for the generated DomainResource
 * that bootstrap the dom tree with the domain object
 * and add a few sub resources like log viewer
 * or log-level setup which are not described as configbeans
 * but more external config or files (server.log or JDK logger setup
 * 
 * @author ludo
 */
public class GlassFishDomainResource extends TemplateRestResource {

    public GlassFishDomainResource() {
        //moved init code in the setHabitat callback from Jersey, to get the correct habitat
        //otherwise we cannot used jersey injected values in a constructor (which does not have a param)
    }

    //called when jersey is injecting the habitat...
    @Context
    public void setHabitat(Habitat hab) {
        Dom dom1 = Dom.unwrap(hab.getComponent(Domain.class));
        childModel = dom1.document.getRoot().model;
        entity = dom1.document.getRoot();
    }
}
