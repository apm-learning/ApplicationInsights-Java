/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.microsoft.applicationinsights.internal.quickpulse;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;

class DefaultQuickPulseCoordinatorTest {
    @Test
    void testOnlyPings() throws InterruptedException {
        QuickPulseDataFetcher mockFetcher = mock(QuickPulseDataFetcher.class);
        QuickPulseDataSender mockSender = mock(QuickPulseDataSender.class);
        QuickPulsePingSender mockPingSender = mock(QuickPulsePingSender.class);
        Mockito.doReturn(new QuickPulseHeaderInfo(QuickPulseStatus.QP_IS_OFF)).when(mockPingSender).ping(null);

        QuickPulseCoordinatorInitData initData = new QuickPulseCoordinatorInitDataBuilder()
                .withDataFetcher(mockFetcher)
                .withDataSender(mockSender)
                .withPingSender(mockPingSender)
                .withWaitBetweenPingsInMS(10L)
                .withWaitBetweenPostsInMS(10L)
                .withWaitOnErrorInMS(10L)
                .build();

        DefaultQuickPulseCoordinator coordinator = new DefaultQuickPulseCoordinator(initData);
        Thread thread = new Thread(coordinator);
        thread.setDaemon(true);
        thread.start();

        Thread.sleep(1000);
        coordinator.stop();

        thread.join();

        Mockito.verify(mockFetcher, Mockito.never()).prepareQuickPulseDataForSend(null);

        Mockito.verify(mockSender, Mockito.never()).startSending();
        Mockito.verify(mockSender, Mockito.never()).getQuickPulseHeaderInfo();

        Mockito.verify(mockPingSender, Mockito.atLeast(1)).ping(null);
    }

    @Test
    void testOnePingAndThenOnePost() throws InterruptedException {
        QuickPulseDataFetcher mockFetcher = mock(QuickPulseDataFetcher.class);
        QuickPulseDataSender mockSender = mock(QuickPulseDataSender.class);
        Mockito.doReturn(new QuickPulseHeaderInfo(QuickPulseStatus.QP_IS_OFF)).when(mockSender).getQuickPulseHeaderInfo();

        QuickPulsePingSender mockPingSender = mock(QuickPulsePingSender.class);
        Mockito.when(mockPingSender.ping(null)).thenReturn(new QuickPulseHeaderInfo(QuickPulseStatus.QP_IS_ON), new QuickPulseHeaderInfo(QuickPulseStatus.QP_IS_OFF));

        QuickPulseCoordinatorInitData initData = new QuickPulseCoordinatorInitDataBuilder()
                .withDataFetcher(mockFetcher)
                .withDataSender(mockSender)
                .withPingSender(mockPingSender)
                .withWaitBetweenPingsInMS(10L)
                .withWaitBetweenPostsInMS(10L)
                .withWaitOnErrorInMS(10L)
                .build();

        DefaultQuickPulseCoordinator coordinator = new DefaultQuickPulseCoordinator(initData);
        Thread thread = new Thread(coordinator);
        thread.setDaemon(true);
        thread.start();

        Thread.sleep(1000);
        coordinator.stop();

        thread.join();

        Mockito.verify(mockFetcher, Mockito.atLeast(1)).prepareQuickPulseDataForSend(null);

        Mockito.verify(mockSender, Mockito.times(1)).startSending();
        Mockito.verify(mockSender, Mockito.times(1)).getQuickPulseHeaderInfo();

        Mockito.verify(mockPingSender, Mockito.atLeast(1)).ping(null);
    }

    @Test
    void testOnePingAndThenOnePostWithRedirectedLink() throws InterruptedException {
        QuickPulseDataFetcher mockFetcher = Mockito.mock(QuickPulseDataFetcher.class);
        QuickPulseDataSender mockSender = Mockito.mock(QuickPulseDataSender.class);
        QuickPulsePingSender mockPingSender = Mockito.mock(QuickPulsePingSender.class);

        Mockito.doNothing().when(mockFetcher).prepareQuickPulseDataForSend((String)notNull());
        Mockito.doReturn(new QuickPulseHeaderInfo(QuickPulseStatus.QP_IS_ON, "https://new.endpoint.com", 100)).when(mockPingSender).ping(any());
        Mockito.doReturn(new QuickPulseHeaderInfo(QuickPulseStatus.QP_IS_OFF, "https://new.endpoint.com", 400)).when(mockSender).getQuickPulseHeaderInfo();

        QuickPulseCoordinatorInitData initData = new QuickPulseCoordinatorInitDataBuilder()
                .withDataFetcher(mockFetcher)
                .withDataSender(mockSender)
                .withPingSender(mockPingSender)
                .withWaitBetweenPingsInMS(10L)
                .withWaitBetweenPostsInMS(10L)
                .withWaitOnErrorInMS(10L)
                .build();

        DefaultQuickPulseCoordinator coordinator = new DefaultQuickPulseCoordinator(initData);
        Thread thread = new Thread(coordinator);
        thread.setDaemon(true);
        thread.start();

        Thread.sleep(1100);
        coordinator.stop();

        thread.join();

        Mockito.verify(mockFetcher, Mockito.atLeast(1)).prepareQuickPulseDataForSend("https://new.endpoint.com");
        Mockito.verify(mockPingSender, Mockito.atLeast(1)).ping(null);
        Mockito.verify(mockPingSender, Mockito.times(2)).ping("https://new.endpoint.com");
    }
}