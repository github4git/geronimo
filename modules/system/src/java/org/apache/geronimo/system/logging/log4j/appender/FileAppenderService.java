/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.system.logging.log4j.appender;

import java.io.File;

import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.log4j.FileAppender;


/**
 * An extention of the default Log4j FileAppenderService which
 * will make the directory structure for the set log file.
 *
 * @version $Revision: 1.4 $ $Date: 2004/03/10 09:59:30 $
 */
public class FileAppenderService extends AbstractAppenderService {
    private final ServerInfo serverInfo;
    private String file;
    private boolean running = false;

    public FileAppenderService(ServerInfo serverInfo) {
        this(serverInfo, new FileAppender());
    }

    public FileAppenderService(ServerInfo serverInfo, FileAppender appender) {
        super(appender);
        this.serverInfo = serverInfo;
    }

    public boolean getAppend() {
        return ((FileAppender) appender).getAppend();
    }

    public void setAppend(boolean append) {
        ((FileAppender) appender).setAppend(append);
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
        if (running) {
            setAppenderFile(file);
        }
    }

    public void doStart() {
        running = true;
        setAppenderFile(file);
        super.doStart();
    }

    public void doStop() {
        super.doStop();
        running = false;
    }

    private void setAppenderFile(String file) {
        file = serverInfo.resolvePath(file);
        new File(file).getParentFile().mkdirs();
        ((FileAppender) appender).setFile(file);
    }

    public boolean getBufferedIO() {
        return ((FileAppender) appender).getBufferedIO();
    }

    public void setBufferedIO(boolean bufferedIO) {
        ((FileAppender) appender).setBufferedIO(bufferedIO);
    }

    public int getBufferSize() {
        return ((FileAppender) appender).getBufferSize();
    }

    public void setBufferSize(int bufferSize) {
        ((FileAppender) appender).setBufferSize(bufferSize);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(FileAppenderService.class.getName(), AbstractAppenderService.GBEAN_INFO);
        infoFactory.setConstructor(new GConstructorInfo(
                new String[]{"ServerInfo"},
                new Class[]{ServerInfo.class}
        ));
        infoFactory.addReference(new GReferenceInfo("ServerInfo", ServerInfo.class.getName()));
        infoFactory.addAttribute(new GAttributeInfo("Append", true));
        infoFactory.addAttribute(new GAttributeInfo("File", true));
        infoFactory.addAttribute(new GAttributeInfo("BufferedIO", true));
        infoFactory.addAttribute(new GAttributeInfo("BufferedSize", true));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
