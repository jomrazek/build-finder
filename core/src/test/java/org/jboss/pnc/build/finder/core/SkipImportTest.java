/*
 * Copyright (C) 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.build.finder.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jboss.pnc.build.finder.koji.ClientSession;
import org.jboss.pnc.build.finder.koji.KojiBuild;
import org.junit.jupiter.api.Test;

import com.redhat.red.build.koji.KojiClientException;

class SkipImportTest {
    @Test
    void verifyMultiImportsKeepEarliest() throws KojiClientException {
        final String checksum = "2e7e85f0ee97afde716231a6c792492a";
        final List<String> filenames = Collections.singletonList("commons-lang-2.6-redhat-2.jar");
        ClientSession session = new MockKojiClientSession("skip-import-test");
        BuildConfig config = new BuildConfig();
        BuildFinder finder = new BuildFinder(session, config);
        Map<String, Collection<String>> checksumTable = Collections.singletonMap(checksum, filenames);
        Map<BuildSystemInteger, KojiBuild> builds = finder.findBuildsSlow(checksumTable);

        assertEquals(2, builds.size());
        assertTrue(builds.containsKey(new BuildSystemInteger(0)));
        assertTrue(builds.containsKey(new BuildSystemInteger(228994, BuildSystem.koji)));
        assertFalse(builds.containsKey(new BuildSystemInteger(251444, BuildSystem.koji)));
    }
}
