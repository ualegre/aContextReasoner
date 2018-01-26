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

package edu.casetools.icase.custom;

import android.content.Context;

import edu.casetools.icase.custom.situations.BatterySOI;
import edu.casetools.icase.custom.situations.IndoorsOutdoorsSOI;
import edu.casetools.icase.custom.situations.NavAssistanceSOI;
import edu.casetools.icase.custom.situations.OldWeatherSOI;
import edu.casetools.icase.custom.situations.StandStillSOI;
import edu.casetools.icase.custom.situations.WeatherSOI;

import uk.co.deansserver.acontextreasoner.ContextReasonerCore;
import uk.co.deansserver.acontextreasoner.logging.DataLogger;
import uk.co.deansserver.acontextreasoner.reasoner.AbstractContextMapper;
import uk.co.deansserver.acontextreasoner.reasoner.ReasonerManager;

import java.util.ArrayList;
import java.util.Map;

/**
 * Temporary class for mapping contexts to components and rules for pilot 1
 *
 * @author Dean Kramer <deankramer99@gmail.com> and Unai Alegre-Ibarra <u.alegre@mdx.ac.uk>
 */
public class CustomContextMapper extends AbstractContextMapper {



    public CustomContextMapper(ContextReasonerCore crc, ReasonerManager rm, Context con) {
        super("CustomContextMapper",crc,rm,con);
        initialiseSituationsOfInterest();
    }

    private void initialiseSituationsOfInterest() {
        situationsOfInterest.add(new BatterySOI("battery"));
        situationsOfInterest.add(new WeatherSOI("weather"));
        situationsOfInterest.add(new StandStillSOI("standstill"));
        situationsOfInterest.add(new NavAssistanceSOI("navassistance"));
        situationsOfInterest.add(new IndoorsOutdoorsSOI("indoorOutdoor"));
        situationsOfInterest.add(new OldWeatherSOI("oldweather"));
    }

}
