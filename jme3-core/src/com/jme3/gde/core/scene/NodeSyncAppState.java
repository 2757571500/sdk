/*
 * Copyright (c) 2003-2012 jMonkeyEngine
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * 
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.gde.core.scene;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.gde.core.sceneexplorer.nodes.AbstractSceneExplorerNode;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;

/**
 *
 * @author normenhansen
 */
public class NodeSyncAppState extends AbstractAppState implements LookupListener {

    private final List<AbstractSceneExplorerNode> newNodes = Collections.synchronizedList(new LinkedList<AbstractSceneExplorerNode>());
    private final List<AbstractSceneExplorerNode> oldNodes = Collections.synchronizedList(new LinkedList<AbstractSceneExplorerNode>());
    private final Result<AbstractSceneExplorerNode> nodeSelectionResult;
    private AbstractSceneExplorerNode node;
    private float timeStep = 1;
    private float timer = 0;

    public NodeSyncAppState() {
        nodeSelectionResult = Utilities.actionsGlobalContext().lookupResult(AbstractSceneExplorerNode.class);
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        nodeSelectionResult.addLookupListener(this);
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        synchronized (newNodes) {
            for (Iterator<AbstractSceneExplorerNode> it = newNodes.iterator(); it.hasNext();) {
                AbstractSceneExplorerNode abstractSceneExplorerNode = it.next();
                abstractSceneExplorerNode.syncSceneData();
                it.remove();
            }
        }
        timer += tpf;
        if (timer > timeStep) {
            timer = 0;
            AbstractSceneExplorerNode node = this.node;
            if (initialized && node != null) {
                node.syncSceneData();
            }
        }
        synchronized (oldNodes) {
            for (Iterator<AbstractSceneExplorerNode> it = oldNodes.iterator(); it.hasNext();) {
                AbstractSceneExplorerNode abstractSceneExplorerNode = it.next();
                abstractSceneExplorerNode.syncSceneData();
                it.remove();
            }
        }
    }

    public void resultChanged(LookupEvent ev) {
        Collection collection = nodeSelectionResult.allInstances();
        AbstractSceneExplorerNode newNode = null;
        for (Iterator it = collection.iterator(); it.hasNext();) {
            Object object = it.next();
            if (object instanceof AbstractSceneExplorerNode) {
                if (object != null) {
                    synchronized (newNodes) {
                        newNodes.add((AbstractSceneExplorerNode) object);
                    }
                }
                newNode = (AbstractSceneExplorerNode) object;
            }
        }
        if (node != null) {
            synchronized (oldNodes) {
                oldNodes.add(node);
            }
        }
        node = newNode;
    }

    @Override
    public void cleanup() {
        nodeSelectionResult.removeLookupListener(this);
        super.cleanup();
    }
}