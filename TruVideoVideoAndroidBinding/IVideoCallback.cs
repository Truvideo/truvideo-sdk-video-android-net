using Android.Runtime;
using Java.Interop;

namespace TruVideoVideoAndroidBinding;

[Register("com.truvideo.video.VideoCallback")]
public interface IVideoCallback : IJavaObject, IDisposable
{
    [Export("onSuccess")]
    void OnSuccess(string result);

    [Export("onFailure")]
    void OnFailure(string error);
}