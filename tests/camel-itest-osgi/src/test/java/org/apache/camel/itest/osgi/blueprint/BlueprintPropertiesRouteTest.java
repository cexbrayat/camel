/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.itest.osgi.blueprint;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.Constants;

import static org.ops4j.pax.exam.OptionUtils.combine;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.scanFeatures;
import static org.ops4j.pax.swissbox.tinybundles.core.TinyBundles.newBundle;

/**
 *
 */
@RunWith(JUnit4TestRunner.class)
public class BlueprintPropertiesRouteTest extends OSGiBlueprintTestSupport {

    private String name = BlueprintPropertiesRouteTest.class.getName();

    @Test
    public void testBlueprintProperties() throws Exception {
        // start bundle
        getInstalledBundle(name).start();

        // must use the camel context from osgi
        CamelContext ctx = getOsgiService(CamelContext.class, "(camel.context.symbolicname=" + name + ")", 10000);

        ProducerTemplate myTemplate = ctx.createProducerTemplate();
        myTemplate.start();

        // do our testing
        MockEndpoint foo = ctx.getEndpoint("mock:foo", MockEndpoint.class);
        foo.expectedMessageCount(1);
        MockEndpoint result = ctx.getEndpoint("mock:result", MockEndpoint.class);
        result.expectedMessageCount(1);

        myTemplate.sendBody("direct:start", "Hello World");

        foo.assertIsSatisfied();
        result.assertIsSatisfied();

        myTemplate.stop();
    }

    @Configuration
    public static Option[] configure() throws Exception {

        Option[] options = combine(
                getDefaultCamelKarafOptions(),

                bundle(newBundle()
                        .add("OSGI-INF/blueprint/test.xml", BlueprintPropertiesRouteTest.class.getResource("blueprint-17.xml"))
                        .set(Constants.BUNDLE_SYMBOLICNAME, BlueprintPropertiesRouteTest.class.getName())
                        .set(Constants.BUNDLE_VERSION, "1.0.0")
                        .build()).noStart(),

                // using the features to install the camel components
                scanFeatures(getCamelKarafFeatureUrl(),
                                    "camel-blueprint"));

        return options;
    }

}
