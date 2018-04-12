package com.droid.sxbot;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * Created by lisongting on 2018/4/11.
 */

public class ItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private ItemTouchHelperCallbackListener listener;

    public ItemTouchHelperCallback() {
    }

    public interface ItemTouchHelperCallbackListener{
        void onMove(RecyclerView.ViewHolder srcViewHolder, RecyclerView.ViewHolder destViewHolder);
        void onSwiped(RecyclerView.ViewHolder viewHolder);
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }


    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int swipeFlag = ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT;
        int dragFlag = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        return makeMovementFlags(dragFlag, swipeFlag);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        if (listener != null) {
            listener.onMove(viewHolder, target);
            return true;
        }
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        if (listener != null) {
            listener.onSwiped(viewHolder);
        }
    }

    public void setItemTouchHelperCallbackListener(ItemTouchHelperCallbackListener listener) {
        this.listener = listener;
    }

}
