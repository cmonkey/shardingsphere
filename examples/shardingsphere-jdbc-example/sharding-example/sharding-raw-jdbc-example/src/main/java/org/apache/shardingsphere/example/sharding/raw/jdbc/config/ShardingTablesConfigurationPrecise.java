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

package org.apache.shardingsphere.example.sharding.raw.jdbc.config;

import org.apache.shardingsphere.example.algorithm.StandardModuloShardingTableAlgorithm;
import org.apache.shardingsphere.example.config.ExampleConfiguration;
import org.apache.shardingsphere.example.core.api.DataSourceUtil;
import org.apache.shardingsphere.sharding.api.config.KeyGeneratorConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.strategy.algorithm.keygen.SnowflakeKeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.RuleConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

public final class ShardingTablesConfigurationPrecise implements ExampleConfiguration {
    
    @Override
    public DataSource getDataSource() throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(getOrderTableRuleConfiguration());
        shardingRuleConfig.getTables().add(getOrderItemTableRuleConfiguration());
        shardingRuleConfig.getBindingTableGroups().add("t_order, t_order_item");
        shardingRuleConfig.getBroadcastTables().add("t_address");
        shardingRuleConfig.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", new StandardModuloShardingTableAlgorithm()));
        Collection<RuleConfiguration> configurations = new LinkedList<>();
        configurations.add(shardingRuleConfig);
        return ShardingSphereDataSourceFactory.createDataSource(createDataSourceMap(), configurations, new Properties());
    }
    
    private static ShardingTableRuleConfiguration getOrderTableRuleConfiguration() {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("t_order", "demo_ds.t_order_${[0, 1]}");
        result.setKeyGenerator(new KeyGeneratorConfiguration("order_id", getSnowflakeKeyGenerateAlgorithm()));
        return result;
    }
    
    private static ShardingTableRuleConfiguration getOrderItemTableRuleConfiguration() {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("t_order_item", "demo_ds.t_order_item_${[0, 1]}");
        result.setKeyGenerator(new KeyGeneratorConfiguration("order_item_id", getSnowflakeKeyGenerateAlgorithm()));
        return result;
    }
    
    private static Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>();
        result.put("demo_ds", DataSourceUtil.createDataSource("demo_ds"));
        return result;
    }
    
    private static KeyGenerateAlgorithm getSnowflakeKeyGenerateAlgorithm() {
        KeyGenerateAlgorithm result = new SnowflakeKeyGenerateAlgorithm();
        result.setProperties(getProperties());
        return result;
    }
    
    private static Properties getProperties() {
        Properties result = new Properties();
        result.setProperty("worker.id", "123");
        return result;
    }
}
