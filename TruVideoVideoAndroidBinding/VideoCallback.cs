namespace TruVideoVideoAndroidBinding;

public class VideoCallback : Java.Lang.Object, TruVideoVideoAndroid.IVideoCallback
{
    private readonly Action<string> _onSuccess;
    private readonly Action<string> _onFailure;

    public VideoCallback(Action<string> onSuccess, Action<string> onFailure) {
        _onSuccess = onSuccess;
        _onFailure = onFailure;
    }

    public void OnSuccess(string result) {
        _onSuccess?.Invoke(result);
    }

    public void OnFailure(string error) {
        _onFailure?.Invoke(error);
    }
}