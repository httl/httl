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
 */
package httl.spi.loaders;

import httl.Resource;
import httl.spi.Configurable;
import httl.spi.Loader;
import httl.util.ClassUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * MultiLoader. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.Engine#setLoader(Loader)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class MultiLoader implements Loader, Configurable {

    private final List<Loader> templateLoaders = new CopyOnWriteArrayList<Loader>();
    
	public void configure(Map<String, String> config) {
	    String value = config.get(LOADERS);
        if (value != null && value.trim().length() > 0) {
            String[] values = value.trim().split("[\\s\\,]+");
            Loader[] loaders = new Loader[values.length];
            for (int i = 0; i < values.length; i ++) {
                loaders[i] = (Loader) ClassUtils.newInstance(values[i]);
                if (loaders[i] instanceof Configurable) {
                    ((Configurable)loaders[i]).configure(config);
                }
            }
            add(loaders);
        }
	}

    public void add(Loader... loaders) {
        if (loaders != null && loaders.length > 0) {
            for (Loader loader : loaders) {
                if (loader != null && ! templateLoaders.contains(loader)) {
                    templateLoaders.add(loader);
                }
            }
        }
    }
    
    public void remove(Loader... loaders) {
        if (loaders != null && loaders.length > 0) {
            for (Loader loader : loaders) {
                if (loader != null) {
                    templateLoaders.remove(loader);
                }
            }
        }
    }
    
    public void clear() {
        templateLoaders.clear();
    }

    public Resource load(String name, String encoding) throws IOException {
        for (Loader loader : templateLoaders) {
            try {
                return loader.load(name, encoding);
            } catch (Exception e) {
            }
        }
        throw new FileNotFoundException("No such template file: " + name);
    }

    public List<String> list() {
        List<String> all = new ArrayList<String>();
        for (Loader loader : templateLoaders) {
            try {
                List<String> list = loader.list();
                if (list != null && list.size() > 0) {
                    all.addAll(list);
                }
            } catch (Exception e) {
            }
        }
        return all;
    }

}
