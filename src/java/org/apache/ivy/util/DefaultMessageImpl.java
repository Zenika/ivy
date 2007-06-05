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
package org.apache.ivy.util;

public class DefaultMessageImpl implements MessageImpl {
    private int _level = Message.MSG_INFO;

    /**
     * @param level
     */
    public DefaultMessageImpl(int level) {
        _level = level;
    }

    public void log(String msg, int level) {
        if (level <= _level) {
            System.out.println(msg);
        }
    }

    public void rawlog(String msg, int level) {
        log(msg, level);
    }

    public void progress() {
        System.out.print(".");
    }

    public void endProgress(String msg) {
        System.out.println(msg);
    }

    public int getLevel() {
        return _level;
    }
}
