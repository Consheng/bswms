package ykk.xc.com.bswms.entrance.page5;//package ykk.cb.com.zcws.basics;
//
//import android.app.Activity;
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.support.v7.widget.DividerItemDecoration;
//import android.support.v7.widget.LinearLayoutManager;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//
//import java.io.IOException;
//import java.lang.ref.WeakReference;
//import java.util.ArrayList;
//import java.util.List;
//
//import butterknife.BindView;
//import butterknife.OnClick;
//import okhttp3.Call;
//import okhttp3.Callback;
//import okhttp3.FormBody;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.Response;
//import okhttp3.ResponseBody;
//import ykk.cb.com.zcws.R;
//import ykk.cb.com.zcws.basics.adapter.PrintFragment3Adapter;
//import ykk.cb.com.zcws.comm.BaseFragment;
//import ykk.cb.com.zcws.comm.Comm;
//import ykk.cb.com.zcws.bean.Box;
//import ykk.cb.com.zcws.bean.BoxBarCode;
//import ykk.cb.com.zcws.util.JsonUtil;
//import ykk.cb.com.zcws.util.xrecyclerview.XRecyclerView;
//
//public class PrintFragment3 extends BaseFragment implements XRecyclerView.LoadingListener {
//
//    @BindView(R.id.et_search)
//    EditText etSearch;
//    @BindView(R.id.btn_search)
//    Button btnSearch;
//    @BindView(R.id.tv_print_type)
//    TextView tvPrintType;
//    @BindView(R.id.xRecyclerView)
//    XRecyclerView xRecyclerView;
//
//    private PrintFragment3 context = this;
//    private List<Box> listDatas = new ArrayList<>();
//    private static final int SUCC1 = 200, UNSUCC1 = 500, SUCC2 = 201, UNSUCC2 = 501;
//    private static final int CODE1 = 1;
//    private PrintFragment3Adapter mAdapter;
//    private OkHttpClient okHttpClient = new OkHttpClient();
//    private Activity mContext;
//    private PrintMainActivity parent;
//    private int limit = 1;
//    private boolean isRefresh, isLoadMore, isNextPage;
//    private int curId; // ?????????
//
//    // ????????????
//    final PrintFragment3.MyHandler mHandler = new PrintFragment3.MyHandler(this);
//    private static class MyHandler extends Handler {
//        private final WeakReference<PrintFragment3> mActivity;
//
//        public MyHandler(PrintFragment3 activity) {
//            mActivity = new WeakReference<PrintFragment3>(activity);
//        }
//
//        public void handleMessage(Message msg) {
//            PrintFragment3 m = mActivity.get();
//            if (m != null) {
//                m.hideLoadDialog();
//
//                switch (msg.what) {
//                    case SUCC1: // ??????
//                        String json = (String) msg.obj;
//                        List<Box> list = JsonUtil.strToList2(json, Box.class);
//                        m.listDatas.addAll(list);
//                        m.mAdapter.notifyDataSetChanged();
//
//                        if (m.isRefresh) {
//                            m.xRecyclerView.refreshComplete(true);
//                        } else if (m.isLoadMore) {
//                            m.xRecyclerView.loadMoreComplete(true);
//                        }
//
//                        m.xRecyclerView.setPullRefreshEnabled(true); // ??????????????????
//                        m.xRecyclerView.setLoadingMoreEnabled(m.isNextPage);
//
//                        break;
//                    case UNSUCC1: // ?????????????????????
//                        m.toasts("?????????????????????????????????");
//
//                        break;
//                    case SUCC2: // ???????????? ??????
//                        List<BoxBarCode> list2 = JsonUtil.strToList((String) msg.obj, BoxBarCode.class);
//                        m.parent.setFragmentPrint3(3, list2);
//
//                        break;
//                    case UNSUCC2: // ?????????????????????
//                        String str = JsonUtil.strToString((String) msg.obj);
//                        Comm.showWarnDialog(m.mContext, str);
//
//                        break;
//                }
//            }
//        }
//    }
//
//    @Override
//    public View setLayoutResID(LayoutInflater inflater, ViewGroup container) {
//        return inflater.inflate(R.layout.ab_print_fragment3, container, false);
//    }
//
//    @Override
//    public void initView() {
//        mContext = getActivity();
//        parent = (PrintMainActivity) mContext;
//
//        xRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
//        xRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
//        mAdapter = new PrintFragment3Adapter(mContext, listDatas);
//        xRecyclerView.setAdapter(mAdapter);
//        xRecyclerView.setLoadingListener(context);
//
//        xRecyclerView.setPullRefreshEnabled(false); // ??????????????????
//        xRecyclerView.setLoadingMoreEnabled(false); // ????????????????????????view
//
//        mAdapter.setCallBack(new PrintFragment3Adapter.MyCallBack() {
//            @Override
//            public void onPrint(Box e, int pos) {
//                Log.e("onPrint3", e.getBoxName());
//                curId = e.getId();
//                showInputDialog("????????????", "", "0", CODE1);
//            }
//        });
//    }
//
//    @Override
//    public void initData() {
////        initLoadDatas();
//    }
//
//    @OnClick({R.id.btn_search})
//    public void onViewClicked(View v) {
//        switch (v.getId()) {
//            case R.id.btn_search: // ????????????
//                initLoadDatas();
//
//                break;
//        }
//    }
//
//    private void initLoadDatas() {
//        limit = 1;
//        listDatas.clear();
//        run_okhttpDatas();
//    }
//
//    /**
//     * ??????okhttp????????????
//     * ????????????????????????????????????????????????
//     */
//    private void run_okhttpDatas() {
//        showLoadDialog("?????????...");
//        String mUrl = getURL("box/findBoxByList_app");
//        String searchName = getValues(etSearch).trim();
//        FormBody formBody = new FormBody.Builder()
//                .add("NameAndSize", getValues(etSearch).trim())
//                .add("limit", String.valueOf(limit))
//                .add("pageSize", "30")
//                .build();
//
//        Request request = new Request.Builder()
//                .addHeader("cookie", getSession())
//                .url(mUrl)
//                .post(formBody)
//                .build();
//
//        // step 3????????? Call ??????
//        Call call = okHttpClient.newCall(request);
//
//        //step 4: ??????????????????
//        call.enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                mHandler.sendEmptyMessage(UNSUCC1);
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                ResponseBody body = response.body();
//                String result = body.string();
//                if(!JsonUtil.isSuccess(result)) {
//                    mHandler.sendEmptyMessage(UNSUCC1);
//                    return;
//                }
//                isNextPage = JsonUtil.isNextPage(result);
//
//                Message msg = mHandler.obtainMessage(SUCC1, result);
//                Log.e("PrintFragment1 --> onResponse", result);
//                mHandler.sendMessage(msg);
//            }
//        });
//    }
//
//    /**
//     * ??????okhttp????????????
//     * ????????????????????????????????????????????????
//     */
//    private void run_CreateAndPrint(int number) {
//        showLoadDialog("????????????...");
//        String mUrl = getURL("appPrint/boxBarCode_CreateAndPrint");
//        String searchName = getValues(etSearch).trim();
//        FormBody formBody = new FormBody.Builder()
//                .add("ids", String.valueOf(curId))
//                .add("number", String.valueOf(number))
//                .build();
//
//        Request request = new Request.Builder()
//                .addHeader("cookie", getSession())
//                .url(mUrl)
//                .post(formBody)
//                .build();
//
//        // step 3????????? Call ??????
//        Call call = okHttpClient.newCall(request);
//
//        //step 4: ??????????????????
//        call.enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                mHandler.sendEmptyMessage(UNSUCC2);
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                ResponseBody body = response.body();
//                String result = body.string();
//                if(!JsonUtil.isSuccess(result)) {
//                    Message msg = mHandler.obtainMessage(UNSUCC2, result);
//                    mHandler.sendMessage(msg);
//                    return;
//                }
//
//                Message msg = mHandler.obtainMessage(SUCC2, result);
//                Log.e("PrintFragment1 --> onResponse", result);
//                mHandler.sendMessage(msg);
//            }
//        });
//    }
//
//    @Override
//    public void onRefresh() {
//        isRefresh = true;
//        isLoadMore = false;
//        initLoadDatas();
//    }
//
//    @Override
//    public void onLoadMore() {
//        isRefresh = false;
//        isLoadMore = true;
//        limit += 1;
//        run_okhttpDatas();
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        switch (requestCode) {
//            case CODE1: // ??????
//                if (resultCode == Activity.RESULT_OK) {
//                    Bundle bundle = data.getExtras();
//                    if (bundle != null) {
//                        String value = bundle.getString("resultValue", "");
//                        int num = parseInt(value);
//                        run_CreateAndPrint(num);
//                    }
//                }
//
//                break;
//        }
//    }
//
//    @Override
//    public void onDestroyView() {
//        closeHandler(mHandler);
//        mBinder.unbind();
//        super.onDestroyView();
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
////        mContext.unregisterReceiver(mReceiver);
//    }
//}
