package com.netease.cloudmusic.datareport.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.View;

import com.netease.cloudmusic.datareport.R;
import com.netease.cloudmusic.datareport.policy.MenuNode;
import com.netease.cloudmusic.datareport.vtree.VTreeUtilsKt;
import com.netease.cloudmusic.datareport.vtree.logic.LogicMenuManager;


public class DataBinder {

    private static final IDataBinder VIEW_BINDER = new ViewDataBinder();
    private static final IDataBinder MENU_BINDER = new MenuItemDataBinder();

    /**
     * 获取和对象绑定的DataEntity
     */
    @Nullable
    static DataEntity getDataEntity(@Nullable Object object) {
        return object == null ? null : with(object).getDataEntity(object);
    }

    /**
     * 为对象写入DataEntity
     * 考虑到可能调用者想清除DataEntity，所以允许为空的DataEntity被设置进去
     */
    static void setDataEntity(@Nullable Object object, @Nullable DataEntity entity) {
        if (object == null) {
            return;
        }
        with(object).setDataEntity(object, entity);
    }

    static void removeDataEntity(@Nullable Object object) {
        if (object == null) {
            return;
        }
        with(object).removeDataEntity(object);
    }

    @NonNull
    private static IDataBinder with(Object object) {
        if (object instanceof MenuNode) {
            return MENU_BINDER;
        } else {
            return VIEW_BINDER;
        }
    }

    /**
     * 设置和获取DataEntity的接口类
     */
    private interface IDataBinder {

        @Nullable
        DataEntity getDataEntity(Object object);

        void setDataEntity(Object object, DataEntity entity);

        void removeDataEntity(Object object);
    }

    /**
     * 实现对View的数据存取的类
     */
    private static class ViewDataBinder implements IDataBinder {

        @Nullable
        @Override
        public DataEntity getDataEntity(Object object) {
            View view = VTreeUtilsKt.getView(object);
            return view == null ? null : (DataEntity) view.getTag(R.id.key_data_package);
        }

        @Override
        public void setDataEntity(Object object, DataEntity entity) {
            View view = VTreeUtilsKt.getView(object);
            if (view == null) {
                return;
            }
            view.setTag(R.id.key_data_package, entity);
        }

        @Override
        public void removeDataEntity(Object object) {
            View view = VTreeUtilsKt.getView(object);
            if (view == null) {
                return;
            }
            view.setTag(R.id.key_data_package, null);
        }
    }

    private static class MenuItemDataBinder implements IDataBinder {

        @Nullable
        @Override
        public DataEntity getDataEntity(Object object) {
            MenuNode menuNode = (MenuNode) object;
            if (menuNode.getType() == MenuNode.MENU_ITEM_RES) {
                return LogicMenuManager.INSTANCE.getMenuItemDataEntity(menuNode.getActivity(), menuNode.getMenuRes());
            } else if (menuNode.getType() == MenuNode.MENU_PAGE_RES) {
                return LogicMenuManager.INSTANCE.getMenuPageDataEntity(menuNode.getActivity(), menuNode.getMenuRes());
            }
            return null;
        }

        @Override
        public void setDataEntity(Object object, DataEntity entity) {
            MenuNode menuNode = (MenuNode) object;
            if (menuNode.getType() == MenuNode.MENU_ITEM_RES) {
                LogicMenuManager.INSTANCE.setMenuItemDataEntity(menuNode.getActivity(), menuNode.getMenuRes(), entity);
            } else if (menuNode.getType() == MenuNode.MENU_PAGE_RES) {
                LogicMenuManager.INSTANCE.setMenuPageDataEntity(menuNode.getActivity(), menuNode.getMenuRes(), entity);
            }
        }

        @Override
        public void removeDataEntity(Object object) {
            //没有实现
        }
    }
}
