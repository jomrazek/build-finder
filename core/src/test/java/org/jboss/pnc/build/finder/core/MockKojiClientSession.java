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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.commonjava.rwx.api.RWXMapper;
import org.commonjava.rwx.core.Registry;
import org.jboss.pnc.build.finder.koji.ClientSession;

import com.redhat.red.build.koji.KojiClientException;
import com.redhat.red.build.koji.model.generated.Model_Registry;
import com.redhat.red.build.koji.model.xmlrpc.KojiArchiveInfo;
import com.redhat.red.build.koji.model.xmlrpc.KojiArchiveQuery;
import com.redhat.red.build.koji.model.xmlrpc.KojiArchiveType;
import com.redhat.red.build.koji.model.xmlrpc.KojiBuildInfo;
import com.redhat.red.build.koji.model.xmlrpc.KojiIdOrName;
import com.redhat.red.build.koji.model.xmlrpc.KojiRpmInfo;
import com.redhat.red.build.koji.model.xmlrpc.KojiTagInfo;
import com.redhat.red.build.koji.model.xmlrpc.KojiTaskInfo;
import com.redhat.red.build.koji.model.xmlrpc.KojiTaskRequest;
import com.redhat.red.build.koji.model.xmlrpc.messages.GetArchiveTypesResponse;
import com.redhat.red.build.koji.model.xmlrpc.messages.GetBuildResponse;
import com.redhat.red.build.koji.model.xmlrpc.messages.GetTaskRequestResponse;
import com.redhat.red.build.koji.model.xmlrpc.messages.GetTaskResponse;
import com.redhat.red.build.koji.model.xmlrpc.messages.ListArchivesResponse;
import com.redhat.red.build.koji.model.xmlrpc.messages.ListTagsResponse;

public class MockKojiClientSession implements ClientSession {
    private final RWXMapper rwxMapper;

    private final String resourcePath;

    MockKojiClientSession(String resourcePath) {
        Registry.setInstance(new Model_Registry());
        this.rwxMapper = new RWXMapper();
        this.resourcePath = resourcePath;
    }

    private String readResource(String resourceFile) throws IOException {
        String resource = FilenameUtils.separatorsToUnix(FilenameUtils.concat(resourcePath, resourceFile));
        File file = TestUtils.loadFile(resource);

        return FileUtils.readFileToString(file, "UTF-8");
    }

    private <T> T parseCapturedMessage(Class<T> type, String filename) throws Exception {
        String source = readResource(filename);

        return rwxMapper.parse(new ByteArrayInputStream(source.getBytes()), type);
    }

    @Override
    public List<KojiArchiveInfo> listArchives(KojiArchiveQuery query) throws KojiClientException {
        String id = null;

        if (query.getChecksum() != null) {
            id = query.getChecksum();
        } else if (query.getBuildId() != null) {
            id = String.valueOf(query.getBuildId());
        }

        try {
            ListArchivesResponse response = parseCapturedMessage(
                    ListArchivesResponse.class,
                    "listArchives-" + id + ".xml");
            return response.getArchives();
        } catch (Exception e) {
            throw new KojiClientException("listArchives(" + query + ") failed", e);
        }
    }

    @Override
    public Map<String, KojiArchiveType> getArchiveTypeMap() throws KojiClientException {
        try {
            GetArchiveTypesResponse response = parseCapturedMessage(
                    GetArchiveTypesResponse.class,
                    "getArchiveTypes-all.xml");
            Map<String, KojiArchiveType> types = new HashMap<>();
            List<KojiArchiveType> archiveTypes = response.getArchiveTypes();

            for (KojiArchiveType at : archiveTypes) {
                List<String> extensions = at.getExtensions();

                for (String ext : extensions) {
                    types.put(ext, at);
                }
            }

            return types;
        } catch (Exception e) {
            throw new KojiClientException("getArchiveTypesMap() failed", e);
        }
    }

    @Override
    public KojiBuildInfo getBuild(int buildId) throws KojiClientException {
        try {
            GetBuildResponse response = parseCapturedMessage(GetBuildResponse.class, "getBuild-" + buildId + ".xml");
            return response.getBuildInfo();
        } catch (Exception e) {
            throw new KojiClientException("getBuild(" + buildId + ") failed", e);
        }
    }

    @Override
    public KojiTaskInfo getTaskInfo(int taskId, boolean request) throws KojiClientException {
        try {
            GetTaskResponse response = parseCapturedMessage(GetTaskResponse.class, "getTaskInfo-" + taskId + ".xml");
            return response.getTaskInfo();
        } catch (Exception e) {
            throw new KojiClientException("getTaskInfo(" + taskId + ", " + request + ") failed", e);
        }
    }

    @Override
    public KojiTaskRequest getTaskRequest(int taskId) throws KojiClientException {
        try {
            GetTaskRequestResponse response = parseCapturedMessage(
                    GetTaskRequestResponse.class,
                    "getTaskRequest-" + taskId + ".xml");
            return new KojiTaskRequest(response.getTaskRequestInfo());
        } catch (Exception e) {
            throw new KojiClientException("getTaskRequest(" + taskId + ") failed", e);
        }
    }

    @Override
    public List<KojiTagInfo> listTags(int id) throws KojiClientException {
        try {
            ListTagsResponse response = parseCapturedMessage(ListTagsResponse.class, "listTags-" + id + ".xml");
            return response.getTags();
        } catch (Exception e) {
            throw new KojiClientException("getTaskRequest(" + id + ") failed", e);
        }
    }

    @Override
    public void enrichArchiveTypeInfo(List<KojiArchiveInfo> archiveInfos) {

    }

    @Override
    public List<List<KojiArchiveInfo>> listArchives(List<KojiArchiveQuery> queries) {
        return null;
    }

    @Override
    public List<KojiBuildInfo> getBuild(List<KojiIdOrName> idsOrNames) {
        return null;
    }

    @Override
    public List<KojiRpmInfo> getRPM(List<KojiIdOrName> idsOrNames) {
        return null;
    }

    @Override
    public List<KojiTaskInfo> getTaskInfo(List<Integer> taskIds, List<Boolean> requests) {
        return null;
    }

    @Override
    public List<List<KojiRpmInfo>> listBuildRPMs(List<KojiIdOrName> idsOrNames) {
        return null;
    }

    @Override
    public List<List<KojiTagInfo>> listTags(List<KojiIdOrName> idsOrNames) {
        return null;
    }
}
