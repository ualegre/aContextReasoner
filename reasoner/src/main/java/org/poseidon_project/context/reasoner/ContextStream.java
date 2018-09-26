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

import edu.casetools.icase.custom.OntologyManager;
import eu.larkc.csparql.cep.api.RdfQuadruple;
import eu.larkc.csparql.cep.api.RdfStream;

/**
 * The POSEIDON C-SPARQL Context Stream
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class ContextStream extends RdfStream {

    public ContextStream(String iri) {
        super(iri);
    }

    public void sendStream(String subject, String predicate, String value, long time) {
        final RdfQuadruple q = new RdfQuadruple(OntologyManager.BASE_ONTOLOGY + subject, OntologyManager.BASE_ONTOLOGY + predicate, value, time);
        this.put(q);
    }

}
