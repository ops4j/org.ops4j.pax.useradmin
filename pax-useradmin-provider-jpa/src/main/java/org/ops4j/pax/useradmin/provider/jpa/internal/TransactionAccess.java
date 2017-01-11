/*
 * Copyright 2013 OPS4J
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.useradmin.provider.jpa.internal;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 * An internal interface used for acessing transactions in a uniform manner
 * 
 * @param <T>
 */
public interface TransactionAccess<T> {

    T doWork(EntityManager manager, EntityTransaction transaction);

    /**
     * @return what cause to report to the user if an exception is thrown from
     *         the {@link #doWork(EntityManager, EntityTransaction)} method
     */
    String getProblemString();
}
