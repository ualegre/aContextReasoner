/*
 * Copyright 2017 aContextReasoner Project
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

package uk.co.deansserver.acontextreasoner.reasoner;

import eu.larkc.csparql.cep.api.RdfQuadruple;
import eu.larkc.csparql.cep.api.RdfStream;

/**
 * The POSEIDON C-SPARQL Context Stream
 *
 * @author Dean Kramer <deankramer99@gmail.com>
 */
public class ContextStream extends RdfStream {

    private String streamiri = "http://ie.cs.mdx.ac.uk/POSEIDON/";

    public ContextStream(String iri) {
        super(iri);
    }

    public void sendStream(String subject, String predicate, String value, long time) {
        final RdfQuadruple q = new RdfQuadruple(streamiri + subject, streamiri + predicate, value, time);
        this.put(q);
    }

}
