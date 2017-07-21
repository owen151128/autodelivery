package com.hpcnt.autodelivery.network;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.hpcnt.autodelivery.BaseApplication;
import com.hpcnt.autodelivery.StringFetchListener;

public class BuildFetcher {
    private static final String TAG = BuildFetcher.class.getSimpleName();
    private RequestQueue mQueue = BaseApplication.getRequestQueue();

    /*
     * FIXME 이 부분을 ReactiveX + 비동기 부분을 I/O Scheduler + 결과를 subscribe 하는 부분을 Android main thread 에서 받는 구조로 바꿀 수 있음
     * Android main thread scheduler를 RX 용으로 구현한 라이브러리는 RxAndroid 에 있다.
     * 코드상으로는 Volley 를 사용하는 것 보다 당장은 길어지지만, 요구사항 변화에는 좀더 유연해진다.
     * 왜냐면 volley 의 StringRequest 는 기본적으로 버퍼를 모두 채운 문자열을 만들어서 리턴하기 때문에,
     * 매우 큰 HTML document 를 가져오는 상황에서 메모리가 부족하다면 쓸 수 없기 때문이다.
     *
     * Android 4.4 이상 phone 에서는 java.net.HttpUrlConnection 이 내부적으로 OkHttp 를 사용하므로,
     * HttpUrlConnection 을 사용하는 편이 좋다.
     */
    public void fetchBuildList(StringFetchListener buildFetcherListener, String path) {
        StringRequest request = new StringRequest(Method.GET, BaseApplication.BUILD_SERVER_URL + path, response -> {
            buildFetcherListener.onStringFetched(response);
        }, error -> {
            buildFetcherListener.onStringError(error.toString());
        });

        mQueue.add(request);
    }

    /*
     * 수신측에서는 이 Observable 의 cancellation policy 를 크게 두가지 방법으로 결정할 수 있다.
     *
     * 1. Observable 에 subscribe 시 발생하는 subscription(Disposable/Flowable/etc) 을 onDestroy 시점에서 해제
     * 2. Observable 에 takeUntil 연산자를 사용해 takeUntil observable의 시점을 'Activity onDestroy 에 맞출 것)
     *
     * // PSEUDO CODE
     * public Observable<String> fetchBuildList(String path) {
     *     return Observable.create(subscriber ->
     *         UrlConnection connection = new HttpUrlConnection(...)
     *         connection.setMethod("GET")
     *         connection.open(baseUrl + path)
     *         InputStream is = connection.getResponseStream()
     *
     *         // 스트리밍, 버퍼링, response size 등에 대한 고려 필요
     *         String htmlDocument = IOUtils.toString(is)
     *         subscriber.onNext(htmlDocument)
     *         subscriber.onComplete()
     *     )
     *         .subscribeOn(RxSchedulers.io())
     *         .observeOn(RxAndroidSchedulers.main())
     * }
     *
     * call site 에서는 아래와 같이 사용한다.
     *
     * void fetchList() {
     *     buildFetcher.fetchBuildList("/")
     *         .subscribe((String) htmlDocument -> {
     *             // do on success logic
     *         }, (Throwable) throwable -> {
     *             // do on error logic
     *         })
     * }
     */
}
