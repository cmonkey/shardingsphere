/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.proxy;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.proxy.arg.BootstrapArguments;
import org.apache.shardingsphere.proxy.config.ProxyConfigurationLoader;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.init.BootstrapInitializer;
import org.apache.shardingsphere.proxy.init.impl.GovernanceBootstrapInitializer;
import org.apache.shardingsphere.proxy.init.impl.StandardBootstrapInitializer;
import org.excavator.boot.cache.ExtensionHelper;
import org.yaml.snakeyaml.Yaml;
import org.excavator.boot.shardingsphere.infra.ext.Extension;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.SQLException;

/**
 * ShardingSphere-Proxy Bootstrap.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Bootstrap {
    
    /**
     * Main entrance.
     *
     * @param args startup arguments
     * @throws IOException IO exception
     * @throws SQLException SQL exception
     */
    public static void main(final String[] args) throws IOException, SQLException {
        BootstrapArguments bootstrapArgs = new BootstrapArguments(args);
        executeExtensionConfig(bootstrapArgs.getConfigurationPath());
        YamlProxyConfiguration yamlConfig = ProxyConfigurationLoader.load(bootstrapArgs.getConfigurationPath());
        createBootstrapInitializer(yamlConfig).init(yamlConfig, bootstrapArgs.getPort());
    }
    
    private static BootstrapInitializer createBootstrapInitializer(final YamlProxyConfiguration yamlConfig) {
        return null == yamlConfig.getServerConfiguration().getGovernance() ? new StandardBootstrapInitializer() : new GovernanceBootstrapInitializer();
    }
    private static void executeExtensionConfig(String path){
        String extensionConfig = path + "ext.yaml";
        log.info("load extensionConfig = [{}]", extensionConfig);
        Yaml yaml = new Yaml();
        try(InputStream inputStream = new FileInputStream(Bootstrap.class.getResource(extensionConfig).getFile())){
                Extension extension = yaml.loadAs(inputStream, Extension.class);
                log.info("extension config [{}]", extension);
                ExtensionHelper.load(extension.getExt());
            }catch (Exception e){
                log.error("load extension config = [{}] Exception = [{}]", extensionConfig, e);
            }
    }
}
