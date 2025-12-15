// カメラアクセスとビデオ表示
async function startCamera() {
    try {
        // カメラへのアクセスを要求
        const stream = await navigator.mediaDevices.getUserMedia({
            video: {
                width: { ideal: 1280 },
                height: { ideal: 720 },
                facingMode: "user" // フロントカメラを使用
            },
            audio: false
        });

        // video要素を取得
        const videoElement = document.getElementById('input_video');

        if (videoElement) {
            // ストリームをvideo要素に設定
            videoElement.srcObject = stream;
            console.log('カメラが正常に起動しました');
        }
    } catch (error) {
        console.error('カメラへのアクセスエラー:', error);
        alert('カメラにアクセスできませんでした。ブラウザの設定を確認してください。');
    }
}

// ページ読み込み時にカメラを起動
window.addEventListener('DOMContentLoaded', () => {
    startCamera();
});

// ページを離れる時にカメラを停止
window.addEventListener('beforeunload', () => {
    const videoElement = document.getElementById('input_video');
    if (videoElement && videoElement.srcObject) {
        const tracks = videoElement.srcObject.getTracks();
        tracks.forEach(track => track.stop());
    }
});