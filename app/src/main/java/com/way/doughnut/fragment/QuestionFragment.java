package com.way.doughnut.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.way.doughnut.R.id;
import com.way.doughnut.R.layout;
import com.way.doughnut.R.string;

public class QuestionFragment extends Fragment implements OnItemClickListener {
    private static final int QUESTION[] = {string.q_1, string.q_2, string.q_3, string.q_4, string.q_5};
    private static final int ANSWER[] = {string.a_1, string.a_2, string.a_3, string.a_4, string.a_5};
    private ListView mQuestionListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(layout.fragment_help, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mQuestionListView = (ListView) view.findViewById(id.list_view);
        mQuestionListView.setAdapter(new QusetionAdapter());
        mQuestionListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(ANSWER[position]).setPositiveButton(android.R.string.ok, null);
        builder.create().show();
    }

    private class QusetionAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return QUESTION.length;
        }

        @Override
        public Integer getItem(int position) {
            return QUESTION[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = LayoutInflater.from(getActivity()).inflate(layout.item_help, parent, false);
            TextView tv = (TextView) convertView;
            tv.setText(getItem(position));
            return convertView;
        }

    }
}
