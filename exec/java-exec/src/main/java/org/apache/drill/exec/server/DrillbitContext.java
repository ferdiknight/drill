/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.drill.exec.server;

import io.netty.channel.EventLoopGroup;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

import org.apache.drill.common.config.DrillConfig;
import org.apache.drill.exec.compile.CodeCompiler;
import org.apache.drill.exec.coord.ClusterCoordinator;
import org.apache.drill.exec.expr.fn.FunctionImplementationRegistry;
import org.apache.drill.exec.memory.BufferAllocator;
import org.apache.drill.exec.physical.impl.OperatorCreatorRegistry;
import org.apache.drill.exec.planner.PhysicalPlanReader;
import org.apache.drill.exec.proto.CoordinationProtos.DrillbitEndpoint;
import org.apache.drill.exec.rpc.control.Controller;
import org.apache.drill.exec.rpc.control.WorkEventBus;
import org.apache.drill.exec.rpc.data.DataConnectionCreator;
import org.apache.drill.exec.server.options.SystemOptionManager;
import org.apache.drill.exec.store.StoragePluginRegistry;
import org.apache.drill.exec.store.StoragePluginRegistry.DrillSchemaFactory;
import org.apache.drill.exec.store.sys.PStoreProvider;
import org.apache.drill.exec.testing.SimulatedExceptions;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Preconditions;

public class DrillbitContext {
//  private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DrillbitContext.class);

  private final BootStrapContext context;
  private final PhysicalPlanReader reader;
  private final ClusterCoordinator coord;
  private final DataConnectionCreator connectionsPool;
  private final DrillbitEndpoint endpoint;
  private final StoragePluginRegistry storagePlugins;
  private final OperatorCreatorRegistry operatorCreatorRegistry;
  private final Controller controller;
  private final WorkEventBus workBus;
  private final FunctionImplementationRegistry functionRegistry;
  private final SystemOptionManager systemOptions;
  private final PStoreProvider provider;
  private final CodeCompiler compiler;
  private final ExecutorService executor;
  private final SimulatedExceptions simulatedExceptions = new SimulatedExceptions();

  public DrillbitContext(DrillbitEndpoint endpoint, BootStrapContext context, ClusterCoordinator coord,
      Controller controller, DataConnectionCreator connectionsPool, WorkEventBus workBus, PStoreProvider provider,
      ExecutorService executor) {
    Preconditions.checkNotNull(endpoint);
    Preconditions.checkNotNull(context);
    Preconditions.checkNotNull(controller);
    Preconditions.checkNotNull(connectionsPool);
    this.workBus = workBus;
    this.controller = controller;
    this.context = context;
    this.coord = coord;
    this.connectionsPool = connectionsPool;
    this.endpoint = endpoint;
    this.provider = provider;
    this.executor = executor;
    this.storagePlugins = new StoragePluginRegistry(this);
    this.reader = new PhysicalPlanReader(context.getConfig(), context.getConfig().getMapper(), endpoint, storagePlugins);
    this.operatorCreatorRegistry = new OperatorCreatorRegistry(context.getConfig());
    this.systemOptions = new SystemOptionManager(context.getConfig(), provider);
    this.functionRegistry = new FunctionImplementationRegistry(context.getConfig(), systemOptions);
    this.compiler = new CodeCompiler(context.getConfig(), systemOptions);
  }

  public FunctionImplementationRegistry getFunctionImplementationRegistry() {
    return functionRegistry;
  }

  public WorkEventBus getWorkBus() {
    return workBus;
  }

  /**
   * @return the system options manager. It is important to note that this manager only contains options at the
   * "system" level and not "session" level.
   */
  public SystemOptionManager getOptionManager() {
    return systemOptions;
  }

  public DrillbitEndpoint getEndpoint() {
    return endpoint;
  }

  public DrillConfig getConfig() {
    return context.getConfig();
  }

  public Collection<DrillbitEndpoint> getBits() {
    return coord.getAvailableEndpoints();
  }

  public BufferAllocator getAllocator() {
    return context.getAllocator();
  }

  public OperatorCreatorRegistry getOperatorCreatorRegistry() {
    return operatorCreatorRegistry;
  }

  public StoragePluginRegistry getStorage() {
    return this.storagePlugins;
  }

  public EventLoopGroup getBitLoopGroup() {
    return context.getBitLoopGroup();
  }

  public DataConnectionCreator getDataConnectionsPool() {
    return connectionsPool;
  }

  public Controller getController() {
    return controller;
  }

  public MetricRegistry getMetrics() {
    return context.getMetrics();
  }

  public PhysicalPlanReader getPlanReader() {
    return reader;
  }

  public PStoreProvider getPersistentStoreProvider() {
    return provider;
  }

  public DrillSchemaFactory getSchemaFactory() {
    return storagePlugins.getSchemaFactory();
  }

  public ClusterCoordinator getClusterCoordinator() {
    return coord;
  }

  public CodeCompiler getCompiler() {
    return compiler;
  }

  public ExecutorService getExecutor() {
    return executor;
  }

  public SimulatedExceptions getSimulatedExceptions() {
    return simulatedExceptions;
  }
}
