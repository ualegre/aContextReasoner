/*
 * Copyright 2015 POSEIDON Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.poseidon_project.context.reasoner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import eu.larkc.csparql.cep.api.RdfQuadruple;
import eu.larkc.csparql.cep.api.RdfStream;

/**
 * The POSEIDON C-SPARQL Context Stream
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class ContextStream extends RdfStream {

    //For testing
    protected final Logger mLogger = LoggerFactory.getLogger(ContextStream.class);

    private  boolean keepRunning = false;


    public ContextStream(String iri) {
        super(iri);
    }

    public void sendStream(String subject, String predicate, String value) {
        final RdfQuadruple q = new RdfQuadruple(getIRI() + "/" + subject, getIRI() + "/" + predicate, value, System.currentTimeMillis());
        this.put(q);
    }

}
