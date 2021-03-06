/*
 * Copyright 2019 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.titus.runtime.jobmanager.gateway;

import com.netflix.titus.api.jobmanager.model.CallMetadata;
import com.netflix.titus.grpc.protogen.Job;
import com.netflix.titus.grpc.protogen.JobAttributesDeleteRequest;
import com.netflix.titus.grpc.protogen.JobAttributesUpdate;
import com.netflix.titus.grpc.protogen.JobCapacityUpdate;
import com.netflix.titus.grpc.protogen.JobChangeNotification;
import com.netflix.titus.grpc.protogen.JobDescriptor;
import com.netflix.titus.grpc.protogen.JobDisruptionBudgetUpdate;
import com.netflix.titus.grpc.protogen.JobProcessesUpdate;
import com.netflix.titus.grpc.protogen.JobQuery;
import com.netflix.titus.grpc.protogen.JobQueryResult;
import com.netflix.titus.grpc.protogen.JobStatusUpdate;
import com.netflix.titus.grpc.protogen.ObserveJobsQuery;
import com.netflix.titus.grpc.protogen.Task;
import com.netflix.titus.grpc.protogen.TaskAttributesDeleteRequest;
import com.netflix.titus.grpc.protogen.TaskAttributesUpdate;
import com.netflix.titus.grpc.protogen.TaskKillRequest;
import com.netflix.titus.grpc.protogen.TaskMoveRequest;
import com.netflix.titus.grpc.protogen.TaskQuery;
import com.netflix.titus.grpc.protogen.TaskQueryResult;
import reactor.core.publisher.Mono;
import rx.Completable;
import rx.Observable;

public class JobServiceGatewayDelegate implements JobServiceGateway {

    private final JobServiceGateway delegate;

    public JobServiceGatewayDelegate(JobServiceGateway delegate) {
        this.delegate = delegate;
    }

    @Override
    public Observable<String> createJob(JobDescriptor jobDescriptor, CallMetadata callMetadata) {
        return delegate.createJob(jobDescriptor, callMetadata);
    }

    @Override
    public Completable updateJobCapacity(JobCapacityUpdate jobCapacityUpdate) {
        return delegate.updateJobCapacity(jobCapacityUpdate);
    }

    @Override
    public Completable updateJobProcesses(JobProcessesUpdate jobProcessesUpdate) {
        return delegate.updateJobProcesses(jobProcessesUpdate);
    }

    @Override
    public Completable updateJobStatus(JobStatusUpdate statusUpdate) {
        return delegate.updateJobStatus(statusUpdate);
    }

    @Override
    public Mono<Void> updateJobDisruptionBudget(JobDisruptionBudgetUpdate request) {
        return delegate.updateJobDisruptionBudget(request);
    }

    @Override
    public Mono<Void> updateJobAttributes(JobAttributesUpdate request) {
        return delegate.updateJobAttributes(request);
    }

    @Override
    public Mono<Void> deleteJobAttributes(JobAttributesDeleteRequest request) {
        return delegate.deleteJobAttributes(request);
    }

    @Override
    public Observable<Job> findJob(String jobId) {
        return delegate.findJob(jobId);
    }

    @Override
    public Observable<JobQueryResult> findJobs(JobQuery jobQuery) {
        return delegate.findJobs(jobQuery);
    }

    @Override
    public Observable<JobChangeNotification> observeJob(String jobId) {
        return delegate.observeJob(jobId);
    }

    @Override
    public Observable<JobChangeNotification> observeJobs(ObserveJobsQuery query) {
        return delegate.observeJobs(query);
    }

    @Override
    public Completable killJob(String jobId) {
        return delegate.killJob(jobId);
    }

    @Override
    public Observable<Task> findTask(String taskId) {
        return delegate.findTask(taskId);
    }

    @Override
    public Observable<TaskQueryResult> findTasks(TaskQuery taskQuery) {
        return delegate.findTasks(taskQuery);
    }

    @Override
    public Completable killTask(TaskKillRequest taskKillRequest) {
        return delegate.killTask(taskKillRequest);
    }

    @Override
    public Completable updateTaskAttributes(TaskAttributesUpdate attributesUpdate) {
        return delegate.updateTaskAttributes(attributesUpdate);
    }

    @Override
    public Completable deleteTaskAttributes(TaskAttributesDeleteRequest deleteRequest) {
        return delegate.deleteTaskAttributes(deleteRequest);
    }

    @Override
    public Completable moveTask(TaskMoveRequest taskMoveRequest) {
        return delegate.moveTask(taskMoveRequest);
    }
}
