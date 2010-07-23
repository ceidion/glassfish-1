/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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
package com.sun.enterprise.config.serverbeans;

import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.util.StringUtils;
import com.sun.grizzly.config.dom.NetworkListener;
import java.util.List;
import org.glassfish.config.support.GlassFishConfigBean;
import org.glassfish.config.support.PropertyResolver;
import org.jvnet.hk2.config.Dom;

/**
 * The Server.java file is getting pretty bloated.
 * Offload some utilities here.
 * Nothing in here is visible outside this package...
 *
 * @author Byron Nevins
 */
class ServerHelper {

    ServerHelper(Server theServer, Config theConfig) {
        server = theServer;
        config = theConfig;

        if(server == null || config == null)
            throw new IllegalArgumentException();
    }

    int getAdminPort() {
        try {
            if (server == null)
                return -1;

            if (config == null)
                return -1;

            String portString = getAdminPortString(server, config);

            if (portString == null)
                return -1; // get out quick.  it is kosher to call with a null Server

            return Integer.parseInt(portString);
        }
        catch (Exception e) {
            // drop through...
        }
        return -1;
    }

     String getHost() {
        String hostName = null;
        Dom serverDom = Dom.unwrap(server);
        Nodes nodes = serverDom.getHabitat().getComponent(Nodes.class);
        if (server == null || nodes == null) {
            return null;
        }

        // Get it from the node associated with the server
        String nodeName = server.getNode();
        if (StringUtils.ok(nodeName)) {
            Node node = nodes.getNode(nodeName);
            if (node != null) {
                hostName = node.getNodeHost();
            }
            // XXX Hack to get around the fact that the default localhost
            // node entry is malformed
            if (hostName == null && nodeName.equals("localhost")) {
                hostName = "localhost";
            }
        }

        if (StringUtils.ok(hostName)) {
            return hostName;
        }
        else {
            return null;
        }
    }

     boolean isRunning() {

         return false;
     }
    ///////////////////////////////////////////
    ///////////////////  all private below
    ///////////////////////////////////////////
    private String getAdminPortString(Server server, Config config) {
        if (server == null || config == null)
            return null;

        try {
            List<NetworkListener> listeners = config.getNetworkConfig().getNetworkListeners().getNetworkListener();

            for (NetworkListener listener : listeners) {
                if ("admin-listener".equals(listener.getProtocol()))
                    return translatePort(listener, server, config);
            }
        }
        catch (Exception e) {
            // handled below...
        }
        return null;
    }

    private String translatePort(NetworkListener adminListener, Server server, Config config) {
        NetworkListener adminListenerRaw = null;

        try {
            Dom serverDom = Dom.unwrap(server);
            Domain domain = serverDom.getHabitat().getComponent(Domain.class);

            adminListenerRaw = GlassFishConfigBean.getRawView(adminListener);
            String portString = adminListenerRaw.getPort();

            if (!isToken(portString))
                return portString;

            PropertyResolver resolver = new PropertyResolver(domain, server.getName());
            return resolver.getPropertyValue(portString);
        }
        catch (ClassCastException e) {
            //jc: workaround for issue 12354
            // TODO severe error
            return translatePortOld(adminListener.getPort(), server, config);
        }
    }

    private String translatePortOld(String portString, Server server, Config config) {
        if (!isToken(portString))
            return portString;

        // isToken returned true so we are NOT assuming anything below!
        String key = portString.substring(2, portString.length() - 1);

        // check cluster and the cluster's config if applicable
        // bnevins Jul 18, 2010 -- don't botehr this should never be called anymore
        SystemProperty prop = server.getSystemProperty(key);

        if (prop != null) {
            return prop.getValue();
        }

        prop = config.getSystemProperty(key);

        if (prop != null) {
            return prop.getValue();
        }

        return null;
    }

    private static boolean isToken(String s) {
        return s != null
                && s.startsWith("${")
                && s.endsWith("}")
                && s.length() > 3;
    }
    private final Server server;
    private final Config config;
}
