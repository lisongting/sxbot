package com.droid.sxbot.mvp.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.droid.sxbot.R;

import java.util.ArrayList;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String SP_KEY = "group_history";
    private SharedPreferences.Editor spEditor;
    private List<String> groupList;
    private Context context;
    private static final int TYPE_TEXT =1;
    private static final int TYPE_ADD_GROUP = 2;
    private boolean isAddGroup = false;
    private OnItemClickListener onItemClickListener;
    private OnAddGroupListener onAddGroupListener;
    private int itemHeight;
    private String group_names ="zdzn_guest_添加分组";

    //监听选择了哪个分组
    public interface OnItemClickListener{
        void onItemClick(String selectedGroup);
    }

    //监听在创建分组时点了取消还是确定
    public interface OnAddGroupListener{
        void onCheck(boolean isChecked);
    }

    public GroupAdapter(Context context) {
        this.context = context;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        spEditor = sharedPreferences.edit();
        String groupHistory = sharedPreferences.getString(SP_KEY, group_names);
        groupList = new ArrayList<>();
        for(String s:groupHistory.split("_")){
            groupList.add(s);
        }
        Resources res = context.getResources();
        itemHeight = res.getDrawable(R.drawable.ic_arrow_down,null).getMinimumHeight();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        switch (viewType) {
            case TYPE_TEXT:
                return new GroupViewHolder(inflater.inflate(R.layout.group_list_item, parent, false));
            case TYPE_ADD_GROUP:
                View view = inflater.inflate(R.layout.add_group_item_layout, parent, false);
                return new AddGroupViewHolder(view);
            default:
                break;

        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        String s = groupList.get(position);
        if (holder instanceof GroupViewHolder) {
            ((GroupViewHolder) holder).tv.setText(s);
        }
    }

    @Override
    public int getItemCount() {
        return groupList.isEmpty() ? 0 : groupList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(isAddGroup && position == groupList.size()-1){
            return TYPE_ADD_GROUP;
        }
        return TYPE_TEXT;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnAddGroupListener(OnAddGroupListener addGroupListener) {
        this.onAddGroupListener = addGroupListener;
    }

    private void addGroupMode(boolean b){
        isAddGroup = b;
    }

    private void saveGroupList(List<String> list){
        int size = list.size();
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<size;i++) {
            sb.append(list.get(i));
            if (i != size - 1) {
                sb.append("_");
            }
        }
        spEditor.putString(SP_KEY, sb.toString());
        spEditor.commit();
    }

    public class GroupViewHolder extends RecyclerView.ViewHolder {
        public TextView tv;
        public GroupViewHolder(View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tv_simple_group_item);

            tv.setHeight(itemHeight);
            itemView.setMinimumHeight(itemHeight);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getLayoutPosition() == getItemCount() - 1) {
                        addGroupMode(true);
                    } else if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(tv.getText().toString());
                        addGroupMode(false);
                    }
                    notifyDataSetChanged();
                }
            });
        }
    }

    public class AddGroupViewHolder extends RecyclerView.ViewHolder{
        private EditText editText;
        private ImageView btOk;
        private ImageView btCancel;
        public AddGroupViewHolder(View itemView) {
            super(itemView);
            editText = itemView.findViewById(R.id.add_group_text);
            btOk = itemView.findViewById(R.id.add_group_ok);
            btCancel = itemView.findViewById(R.id.add_group_cancel);
            btOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String addGroupName = editText.getEditableText().toString();
                    if (addGroupName.trim().length() == 0) {
                        Toast.makeText(context, "分组名不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }else if (groupList.contains(addGroupName)) {
                        Toast.makeText(context, "该分组已存在", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    addGroupMode(false);
                    String addText = groupList.remove(groupList.size()-1);
                    groupList.add(addGroupName);
                    groupList.add(addText);
                    notifyDataSetChanged();
                    if (onAddGroupListener != null) {
                        onAddGroupListener.onCheck(true);
                    }
                    saveGroupList(groupList);
                    editText.setText("");
                }
            });
            btCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addGroupMode(false);
                    notifyDataSetChanged();
                    if (onAddGroupListener != null) {
                        onAddGroupListener.onCheck(false);
                    }
                    editText.setText("");
                }
            });
        }
    }


}
