package com.mahanthesh.africar.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mahanthesh.africar.model.UserInfo;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<UserInfo> userInfoMutableLiveData;

    public HomeViewModel() {
        userInfoMutableLiveData = new MutableLiveData<>();
        UserInfo userInfo = new UserInfo();
        userInfoMutableLiveData.setValue(userInfo);
    }

    public LiveData<UserInfo> getUserInfo() {
        return userInfoMutableLiveData;
    }

    /**
     * update the assistant values
     */
    public Boolean updateStatus(boolean status) {
        UserInfo userInfo = userInfoMutableLiveData.getValue();
        userInfo.setActive(status);
        userInfoMutableLiveData.setValue(userInfo);
        return userInfoMutableLiveData.getValue().getActive();
    }

    /**
     * update the assistant values
     */
    public Boolean updateUserId(String userId) {
        UserInfo userInfo = userInfoMutableLiveData.getValue();
        userInfo.setUserId(userId);
        userInfoMutableLiveData.setValue(userInfo);
        return userInfoMutableLiveData.getValue().getActive();
    }

}