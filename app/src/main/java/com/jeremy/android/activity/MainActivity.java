package com.jeremy.android.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.jeremy.android.R;
import com.jeremy.android.entity.UserInfo;
import com.jeremy.android.view.InterestView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<UserInfo> userInfos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initDatas();
        initViews();
    }

    private void initDatas() {
        for (int i = 0; i < 20; i++) {
            UserInfo userInfo = new UserInfo();
            userInfo.setName("user" + i);
            if (i % 4 == 0) {
                userInfo.setHeadUrl("http://v1.qzone.cc/avatar/201408/17/14/22/53f04a277d3dd110.jpg%21200x200.jpg");
            } else if (i % 4 == 1) {
                userInfo.setHeadUrl("http://v1.qzone.cc/avatar/201508/10/15/14/55c84f4aedd50525.jpg%21200x200.jpg");
            } else if (i % 4 == 2) {
                userInfo.setHeadUrl("http://up.qqjia.com/z/24/tu29448_6.jpg");
            } else {
                userInfo.setHeadUrl("http://p.3761.com/pic/13101399514879.jpg");
            }
            userInfo.setDistance("0." + i + "km");
            userInfo.setDes("兴趣：KTV、吃货、AJ");
            userInfos.add(userInfo);
        }
    }

    private void initViews() {
        InterestView ivList = (InterestView) findViewById(R.id.iv_list);
        ivList.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return userInfos.size();
            }

            @Override
            public Object getItem(int position) {
                return position;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View child, ViewGroup parent) {
                if (child == null) {
                    child = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
                }

                final UserInfo userInfo = userInfos.get(position);

                View rootV = child.findViewById(R.id.cv_root);
                ImageView imgHeader = (ImageView) child.findViewById(R.id.img_photo);
                TextView tvPos = (TextView) child.findViewById(R.id.tv_pos);
                TextView tvName = (TextView) child.findViewById(R.id.tv_name);
                TextView tvDes = (TextView) child.findViewById(R.id.tv_des);
                TextView tvDistance = (TextView) child.findViewById(R.id.tv_distance);

                rootV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this, userInfo.getName() + "\n" + userInfo.getDes(), Toast.LENGTH_SHORT).show();
                    }
                });
                Glide.with(MainActivity.this).load(userInfo.getHeadUrl()).into(imgHeader);
                tvPos.setText("" + position);
                tvName.setText(userInfo.getName());
                tvDes.setText(userInfo.getDes());
                tvDistance.setText(userInfo.getDistance());
                return child;
            }
        });

        ivList.setiDirectionListener(new InterestView.IDirectionListener() {
            @Override
            public void onDirection(boolean isLeft) {
                Toast.makeText(MainActivity.this, isLeft ? "踩" : "赞", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
