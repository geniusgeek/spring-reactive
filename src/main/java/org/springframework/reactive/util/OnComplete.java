/*
 * Copyright 2002-2015 the original author or authors.
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
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.reactive.util;

/**
 * @author Arjen Poutsma
 */
class OnComplete<T> implements Signal<T> {

	public static final OnComplete INSTANCE = new OnComplete();

	private OnComplete() {
	}

	@Override
	public boolean isComplete() {
		return true;
	}

	@Override
	public boolean isOnNext() {
		return false;
	}

	@Override
	public T next() {
		throw new IllegalStateException();
	}

	@Override
	public boolean isOnError() {
		return false;
	}

	@Override
	public Throwable error() {
		throw new IllegalStateException();
	}

}