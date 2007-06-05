/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.ivy.ant;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Properties;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.core.settings.IvyVariableContainer;
import org.apache.ivy.util.Message;
import org.apache.ivy.util.url.CredentialsStore;
import org.apache.ivy.util.url.URLHandler;
import org.apache.ivy.util.url.URLHandlerDispatcher;
import org.apache.ivy.util.url.URLHandlerRegistry;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.DataType;

public class IvyAntSettings extends DataType {

    public static class Credentials {
        private String _realm;

        private String _host;

        private String _username;

        private String _passwd;

        public String getPasswd() {
            return _passwd;
        }

        public void setPasswd(String passwd) {
            _passwd = passwd;
        }

        public String getRealm() {
            return _realm;
        }

        public void setRealm(String realm) {
            _realm = format(realm);
        }

        public String getHost() {
            return _host;
        }

        public void setHost(String host) {
            _host = format(host);
        }

        public String getUsername() {
            return _username;
        }

        public void setUsername(String userName) {
            _username = format(userName);
        }
    }

    private Ivy _ivyEngine = null;

    private File _file = null;

    private URL _url = null;

    private String _realm = null;

    private String _host = null;

    private String _userName = null;

    private String _passwd = null;

    private String id = null;

    /**
     * Returns the default ivy settings of this classloader. If it doesn't exist yet, a new one is
     * created using the given project to back the VariableContainer.
     * 
     * @param project
     * @return
     */
    public static IvyAntSettings getDefaultInstance(Project project) {
        Object defaultInstanceObj = project.getReference("ivy.instance");
        if (defaultInstanceObj != null
                && defaultInstanceObj.getClass().getClassLoader() != IvyAntSettings.class
                        .getClassLoader()) {
            Message
                    .warn("ivy.instance reference an ivy:settings defined in an other classloader.  An new default one will be used in this project.");
            defaultInstanceObj = null;
        }
        if (defaultInstanceObj != null && !(defaultInstanceObj instanceof IvyAntSettings)) {
            throw new BuildException("ivy.instance reference a "
                    + defaultInstanceObj.getClass().getName()
                    + " an not an IvyAntSettings.  Please don't use this reference id ()");
        }
        if (defaultInstanceObj == null) {
            Message
                    .info("No ivy:settings found for the default reference 'ivy.instance'.  A default instance will be used");
            IvyAntSettings defaultInstance = new IvyAntSettings();
            defaultInstance.setProject(project);
            defaultInstance.registerAsDefault();
            return defaultInstance;
        } else {
            return (IvyAntSettings) defaultInstanceObj;
        }

    }

    protected void registerAsDefault() {
        getProject().addReference("ivy.instance", this);
    }

    public File getFile() {
        return _file;
    }

    public URL getUrl() {
        return _url;
    }

    public String getPasswd() {
        return _passwd;
    }

    public void setPasswd(String passwd) {
        _passwd = passwd;
    }

    public String getRealm() {
        return _realm;
    }

    public void setRealm(String realm) {
        _realm = format(realm);
    }

    public String getHost() {
        return _host;
    }

    public void setHost(String host) {
        _host = format(host);
    }

    public String getUsername() {
        return _userName;
    }

    public void setUsername(String userName) {
        _userName = format(userName);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private static String format(String str) {
        return str == null ? str : (str.trim().length() == 0 ? null : str.trim());
    }

    public void addConfiguredCredentials(Credentials c) {
        CredentialsStore.INSTANCE.addCredentials(c.getRealm(), c.getHost(), c.getUsername(), c
                .getPasswd());
    }

    public void setFile(File file) {
        this._file = file;
    }

    public void setUrl(String confUrl) throws MalformedURLException {
        this._url = new URL(confUrl);
    }

    /*
     * public void execute() throws BuildException { ensureMessageInitialised(); if (getId()==null) {
     * log("No id specified for the ivy:settings, set the instance as the default one",
     * Project.MSG_DEBUG); getProject().addReference("ivy.instance", this); } else {
     * getProject().addReference(id, this); } }
     */

    /**
     * @return
     */
    public Ivy getConfiguredIvyInstance() {
        if (_ivyEngine == null) {
            _ivyEngine = createIvyEngine();
        }
        return _ivyEngine;
    }

    private Ivy createIvyEngine() {
        IvyAntVariableContainer ivyAntVariableContainer = new IvyAntVariableContainer(getProject());

        IvySettings settings = new IvySettings(ivyAntVariableContainer);
        // NB: It is alrady done in the ivy.configure, but it is required for
        // defineDefaultSettingFile (that should be done before the ivy.configure
        settings.addAllVariables(getDefaultProperties(), false);

        Ivy ivy = Ivy.newInstance(settings);

        if (_file == null && _url == null) {
            defineDefaultSettingFile(ivyAntVariableContainer);
        }

        try {
            configureURLHandler();
            if (_file != null) {
                if (!_file.exists()) {
                    throw new BuildException("settings file does not exist: " + _file);
                }
                ivy.configure(_file);
            } else {
                if (_url == null) {
                    throw new AssertionError(
                            "ivy setting should have either a file, either an url, and if not defineDefaultSettingFile must set it.");
                }
                ivy.configure(_url);
            }
        } catch (ParseException e) {
            throw new BuildException("impossible to configure ivy:settings with given "
                    + (_file != null ? "file: " + _file : "url :" + _url) + " :" + e, e);
        } catch (IOException e) {
            throw new BuildException("impossible to configure ivy:settings with given "
                    + (_file != null ? "file: " + _file : "url :" + _url) + " :" + e, e);
        }
        return ivy;
    }

    protected Properties getDefaultProperties() {
        URL url = IvySettings.getDefaultPropertiesURL();
        // this is copy of loadURL code from ant Property task (not available in 1.5.1)
        Properties props = new Properties();
        Message.verbose("Loading " + url);
        try {
            InputStream is = url.openStream();
            try {
                props.load(is);
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        } catch (IOException ex) {
            throw new BuildException(ex);
        }
        return props;
    }

    /**
     * Set _file or _url to its default value
     * 
     * @param variableContainer
     */
    private void defineDefaultSettingFile(IvyVariableContainer variableContainer) {
        String settingsFileName = variableContainer.getVariable("ivy.conf.file");
        if (settingsFileName != null) {
            Message.deprecated("'ivy.conf.file' is deprecated, use 'ivy.settings.file' instead");
        } else {
            settingsFileName = variableContainer.getVariable("ivy.settings.file");
        }
        File[] settingsLocations = new File[] {
                new File(getProject().getBaseDir(), settingsFileName),
                new File(getProject().getBaseDir(), "ivyconf.xml"), new File(settingsFileName),
                new File("ivyconf.xml"),};
        for (int i = 0; i < settingsLocations.length; i++) {
            _file = settingsLocations[i];
            Message.verbose("searching settings file: trying " + _file);
            if (_file.exists()) {
                break;
            }
        }
        if (!_file.exists()) {
            if (Boolean.valueOf(getProject().getProperty("ivy.14.compatible")).booleanValue()) {
                Message.info("no settings file found, using Ivy 1.4 default...");
                _file = null;
                _url = IvySettings.getDefault14SettingsURL();
            } else {
                Message.info("no settings file found, using default...");
                _file = null;
                _url = IvySettings.getDefaultSettingsURL();
            }
        }
    }

    private void configureURLHandler() {
        // TODO : the credentialStore should also be scoped
        CredentialsStore.INSTANCE.addCredentials(getRealm(), getHost(), getUsername(), getPasswd());

        URLHandlerDispatcher dispatcher = new URLHandlerDispatcher();
        URLHandler httpHandler = URLHandlerRegistry.getHttp();
        dispatcher.setDownloader("http", httpHandler);
        dispatcher.setDownloader("https", httpHandler);
        URLHandlerRegistry.setDefault(dispatcher);
    }

}
