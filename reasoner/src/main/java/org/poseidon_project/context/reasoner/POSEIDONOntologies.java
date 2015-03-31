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

/**
 * The basic POSEIDON Ontologies that require loading
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class POSEIDONOntologies {

        /*@TODO
        Need to consider what owls we *really* need to load
        as many may not be needed. Imported owls will be loaded anyways
        */

        public static final String[] ONTOLOGIES_ARRAY = {
                //SOUPA Related Ontologies
                "http://pervasive.semanticweb.org/ont/2004/06/action",
                "http://pervasive.semanticweb.org/ont/2004/06/agent",
                "http://pervasive.semanticweb.org/ont/2004/06/bdi",
                "http://pervasive.semanticweb.org/ont/2004/06/device",
                "http://pervasive.semanticweb.org/ont/2004/06/digital-doc",
                "http://pervasive.semanticweb.org/ont/2004/06/document",
                "http://pervasive.semanticweb.org/ont/2004/06/event",
                "http://pervasive.semanticweb.org/ont/2004/06/geo-measurement",
                "http://pervasive.semanticweb.org/ont/2004/06/img-capture",
                "http://pervasive.semanticweb.org/ont/2004/06/knowledge",
                "http://pervasive.semanticweb.org/ont/2004/06/location",
                "http://pervasive.semanticweb.org/ont/2004/06/meeting",
                "http://pervasive.semanticweb.org/ont/2004/06/person",
                "http://pervasive.semanticweb.org/ont/2004/06/policy",
                "http://pervasive.semanticweb.org/ont/2004/06/rcc",
                "http://pervasive.semanticweb.org/ont/2004/06/schedule",
                "http://pervasive.semanticweb.org/ont/2004/06/space",
                "http://pervasive.semanticweb.org/ont/2004/06/time",
                //POSEIDON Ontologies
                "http://ie.cs.mdx.ac.uk/POSEIDON/envir"
        };
}
