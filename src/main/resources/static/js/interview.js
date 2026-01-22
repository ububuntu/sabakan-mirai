/**
 * WebM音声をWAV形式に変換するユーティリティクラス
 */
class AudioConverter {
    /**
     * WebM BlobをWAV形式に変換（16kHzにリサンプリング）
     * @param {Blob} webmBlob WebM形式の音声Blob
     * @returns {Promise<Blob>} WAV形式のBlob
     */
    static async convertWebMToWav(webmBlob) {
        try {
            // WebMをArrayBufferに変換
            const arrayBuffer = await webmBlob.arrayBuffer();

            // AudioContextで音声をデコード
            if (!this.audioContext) {
                this.audioContext = new (window.AudioContext || window.webkitAudioContext)();
            }
            const audioBuffer = await this.audioContext.decodeAudioData(arrayBuffer);

            // 16kHzにリサンプリング
            console.log(`🔄 リサンプリング中: ${audioBuffer.sampleRate}Hz → 16000Hz`);
            const resampled = await this.resampleTo16k(audioBuffer);
            console.log('✅ リサンプリング完了');

            // WAV形式に変換
            return this.audioBufferToWav(resampled);
        } catch (error) {
            console.error('WAV変換エラー:', error);
            throw error;
        }
    }

    /**
     * AudioBufferを16kHzにリサンプリング
     * @param {AudioBuffer} audioBuffer 元の音声バッファ
     * @returns {Promise<AudioBuffer>} 16kHzにリサンプリングされた音声バッファ
     */
    static async resampleTo16k(audioBuffer) {
        const offlineCtx = new OfflineAudioContext(
            audioBuffer.numberOfChannels,
            audioBuffer.duration * 16000,
            16000
        );
        const source = offlineCtx.createBufferSource();
        source.buffer = audioBuffer;
        source.connect(offlineCtx.destination);
        source.start(0);
        return await offlineCtx.startRendering();
    }

    /**
     * AudioBufferをWAV形式のBlobに変換
     * @param {AudioBuffer} audioBuffer 音声バッファ
     * @returns {Blob} WAV形式のBlob
     */
    static audioBufferToWav(audioBuffer) {
        const numberOfChannels = audioBuffer.numberOfChannels;
        const sampleRate = audioBuffer.sampleRate;// PCM
        const bitDepth = 16;

        // インターリーブ処理（ステレオの場合、左右のチャンネルを交互に配置）
        const length = audioBuffer.length * numberOfChannels * 2;
        const buffer = new ArrayBuffer(44 + length);
        const view = new DataView(buffer);

        // WAVヘッダーを書き込み
        this.writeWavHeader(view, audioBuffer, sampleRate, numberOfChannels, bitDepth);

        // 音声データを書き込み
        this.writeWavData(view, audioBuffer, numberOfChannels);

        return new Blob([buffer], { type: 'audio/wav' });
    }

    /**
     * WAVヘッダーを書き込む
     */
    static writeWavHeader(view, audioBuffer, sampleRate, numberOfChannels, bitDepth) {
        const blockAlign = numberOfChannels * bitDepth / 8;
        const byteRate = sampleRate * blockAlign;
        const dataSize = audioBuffer.length * blockAlign;

        let offset = 0;

        // "RIFF" チャンク
        this.writeString(view, offset, 'RIFF'); offset += 4;
        view.setUint32(offset, 36 + dataSize, true); offset += 4;
        this.writeString(view, offset, 'WAVE'); offset += 4;

        // "fmt " サブチャンク
        this.writeString(view, offset, 'fmt '); offset += 4;
        view.setUint32(offset, 16, true); offset += 4; // サブチャンクサイズ
        view.setUint16(offset, 1, true); offset += 2; // オーディオフォーマット (PCM)
        view.setUint16(offset, numberOfChannels, true); offset += 2;
        view.setUint32(offset, sampleRate, true); offset += 4;
        view.setUint32(offset, byteRate, true); offset += 4;
        view.setUint16(offset, blockAlign, true); offset += 2;
        view.setUint16(offset, bitDepth, true); offset += 2;

        // "data" サブチャンク
        this.writeString(view, offset, 'data'); offset += 4;
        view.setUint32(offset, dataSize, true);
    }

    /**
     * WAV音声データを書き込む
     */
    static writeWavData(view, audioBuffer, numberOfChannels) {
        const length = audioBuffer.length;
        let offset = 44;

        // チャンネルデータを取得
        const channels = [];
        for (let i = 0; i < numberOfChannels; i++) {
            channels.push(audioBuffer.getChannelData(i));
        }

        // インターリーブしながら16bitに変換
        for (let i = 0; i < length; i++) {
            for (let channel = 0; channel < numberOfChannels; channel++) {
                const sample = Math.max(-1, Math.min(1, channels[channel][i]));
                view.setInt16(offset, sample < 0 ? sample * 0x8000 : sample * 0x7FFF, true);
                offset += 2;
            }
        }
    }

    /**
     * DataViewに文字列を書き込む
     */
    static writeString(view, offset, string) {
        for (let i = 0; i < string.length; i++) {
            view.setUint8(offset + i, string.charCodeAt(i));
        }
    }
}

/**
 * マイク機能を管理するクラス
 */
class MicrophoneManager {
    constructor(apiBase = '/api') {
        this.apiBase = apiBase;
        this.mediaRecorder = null;
        this.audioChunks = [];
        this.isRecording = false;
        this.recordingInterval = null;
        this.stream = null;
    }

    /**
     * 録音を開始する
     */
    async start() {
        try {
            this.stream = await navigator.mediaDevices.getUserMedia({
                audio: {
                    echoCancellation: true,
                    noiseSuppression: true,
                    autoGainControl: true
                }
            });

            this.mediaRecorder = new MediaRecorder(this.stream, {
                mimeType: 'audio/webm'
            });

            this.audioChunks = [];

            this.mediaRecorder.ondataavailable = (event) => {
                if (event.data.size > 0) {
                    this.audioChunks.push(event.data);
                }
            };

            this.mediaRecorder.onstop = async () => {
                const audioBlob = new Blob(this.audioChunks, { type: 'audio/webm' });
                await this.sendAudioToServer(audioBlob);
                this.audioChunks = [];
            };

            this.mediaRecorder.start();
            this.isRecording = true;

            this.dispatchEvent('recordingStarted');

            // 5秒ごとに音声を送信
            this.recordingInterval = setInterval(() => {
                if (this.mediaRecorder && this.mediaRecorder.state === 'recording') {
                    this.mediaRecorder.stop();
                    this.mediaRecorder.start();
                }
            }, 5000);

            console.log('🎤 録音を開始しました');
            return true;

        } catch (error) {
            console.error('マイクアクセスエラー:', error);
            this.dispatchEvent('recordingError', { error: error.message });
            return false;
        }
    }

    /**
     * 録音を停止する
     */
    async stop() {
        try {
            if (this.mediaRecorder && this.mediaRecorder.state !== 'inactive') {
                this.mediaRecorder.stop();
            }

            if (this.stream) {
                this.stream.getTracks().forEach(track => track.stop());
                this.stream = null;
            }

            if (this.recordingInterval) {
                clearInterval(this.recordingInterval);
                this.recordingInterval = null;
            }

            this.isRecording = false;
            this.dispatchEvent('recordingStopped');

            console.log('⏹️ 録音を停止しました');
            return true;

        } catch (error) {
            console.error('録音停止エラー:', error);
            return false;
        }
    }

    /**
     * 音声データをWAV形式に変換してBase64でサーバーに送信
     */
    async sendAudioToServer(audioBlob) {
        return new Promise(async (resolve, reject) => {
            try {
                // WebMをWAVに変換
                console.log('🔄 WAV形式に変換中...');
                const wavBlob = await AudioConverter.convertWebMToWav(audioBlob);
                console.log('✅ WAV変換完了');

                // Base64に変換
                const reader = new FileReader();

                reader.onloadend = async () => {
                    try {
                        const base64Audio = reader.result.split(',')[1];

                        const response = await fetch(`${this.apiBase}/analyze-audio`, {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify({ audio: base64Audio })
                        });

                        const data = await response.json();
                        console.log('✅ 音声送信成功 (WAV形式):', data);
                        this.dispatchEvent('audioSent', data);
                        resolve(data);

                    } catch (error) {
                        console.error('❌ 音声送信エラー:', error);
                        this.dispatchEvent('audioSendError', { error: error.message });
                        reject(error);
                    }
                };

                reader.onerror = () => reject(new Error('ファイル読み込みエラー'));
                reader.readAsDataURL(wavBlob);

            } catch (error) {
                console.error('❌ WAV変換エラー:', error);
                this.dispatchEvent('audioConvertError', { error: error.message });
                reject(error);
            }
        });
    }

    /**
     * カスタムイベントを発火
     */
    dispatchEvent(eventName, detail = {}) {
        const event = new CustomEvent(`microphone:${eventName}`, { detail });
        window.dispatchEvent(event);
    }

    /**
     * クリーンアップ
     */
    cleanup() {
        if (this.isRecording) {
            this.stop();
        }
    }
}

/**
 * AI分析APIクライアント
 */
class AnalysisAPIClient {
    constructor(apiBase = '/api') {
        this.apiBase = apiBase;
    }

    async testConnection() {
        const response = await fetch(`${this.apiBase}/test`, { method: 'POST' });
        return await response.json();
    }

    async startAnalysis() {
        const response = await fetch(`${this.apiBase}/start`, { method: 'POST' });
        return await response.json();
    }

    async stopAnalysis() {
        const response = await fetch(`${this.apiBase}/stop`, { method: 'POST' });
        return await response.json();
    }

    async reset() {
        const response = await fetch(`${this.apiBase}/reset`, { method: 'POST' });
        return await response.json();
    }

    async getStatus() {
        const response = await fetch(`${this.apiBase}/status`);
        return await response.json();
    }
}

// ========================================
// 音声検出とタイマー管理
// ========================================

// 音声検出の設定
let voiceAnalyser = null;
let voiceAnimationFrame = null;
let silenceTimer = null;
let isSpeaking = false;
const SILENCE_THRESHOLD = 30; // 音量のしきい値（0-100）
const SILENCE_DURATION = 3000; // 無音と判定する時間（ミリ秒）3秒

/**
 * 音声レベルの監視を開始
 */
function startVoiceMonitoring(stream) {
    try {
        const audioContext = new (window.AudioContext || window.webkitAudioContext)();
        voiceAnalyser = audioContext.createAnalyser();
        const source = audioContext.createMediaStreamSource(stream);

        voiceAnalyser.smoothingTimeConstant = 0.8;
        voiceAnalyser.fftSize = 1024;

        source.connect(voiceAnalyser);

        // 音声レベルの監視を開始
        monitorVoiceLevel();
        console.log('✅ 音声レベル監視を開始しました');
    } catch (error) {
        console.error('❌ 音声監視の開始に失敗:', error);
    }
}

/**
 * 音声レベルを継続的に監視
 */
function monitorVoiceLevel() {
    if (!voiceAnalyser) return;

    const bufferLength = voiceAnalyser.frequencyBinCount;
    const dataArray = new Uint8Array(bufferLength);
    voiceAnalyser.getByteFrequencyData(dataArray);

    // 平均音量を計算
    let sum = 0;
    for (let i = 0; i < bufferLength; i++) {
        sum += dataArray[i];
    }
    const average = sum / bufferLength;

    // 音量が閾値を超えているか判定
    if (average > SILENCE_THRESHOLD) {
        // 音声検出
        if (!isSpeaking) {
            console.log('🎤 音声検出: 話し始めました');
            isSpeaking = true;
        }

        // 無音タイマーをリセット
        if (silenceTimer) {
            clearTimeout(silenceTimer);
            silenceTimer = null;
        }
    } else {
        // 無音検出
        if (isSpeaking && !silenceTimer) {
            console.log('🔇 無音検出: タイマー開始（' + (SILENCE_DURATION / 1000) + '秒）');
            silenceTimer = setTimeout(() => {
                console.log('⏱️ 無音が' + (SILENCE_DURATION / 1000) + '秒続きました。次の質問へ');
                isSpeaking = false;
                nextQuestion();
            }, SILENCE_DURATION);
        }
    }

    // 継続的に監視
    voiceAnimationFrame = requestAnimationFrame(monitorVoiceLevel);
}

/**
 * 音声監視を停止
 */
function stopVoiceMonitoring() {
    if (silenceTimer) {
        clearTimeout(silenceTimer);
        silenceTimer = null;
    }

    if (voiceAnimationFrame) {
        cancelAnimationFrame(voiceAnimationFrame);
        voiceAnimationFrame = null;
    }

    voiceAnalyser = null;
    isSpeaking = false;

    console.log('⏹️ 音声監視を停止しました');
}

// ========================================
// メインの面接処理
// ========================================

let microphoneManager;
let apiClient;
let cameraStream;

/**
 * カメラとマイクを起動
 */
async function startCameraAndMicrophone() {
    try {
        // カメラを起動
        cameraStream = await navigator.mediaDevices.getUserMedia({
            video: {
                width: { ideal: 1280 },
                height: { ideal: 720 },
                facingMode: "user"
            },
            audio: false
        });

        const videoElement = document.getElementById('input_video');
        if (videoElement) {
            videoElement.srcObject = cameraStream;
            console.log('✅ カメラが正常に起動しました');
        }

        // マイクマネージャーを初期化
        microphoneManager = new MicrophoneManager('/api');
        apiClient = new AnalysisAPIClient('/api');

        // マイクを自動的に開始
        const micStarted = await microphoneManager.start();
        if (micStarted) {
            updateMicStatus('録音中', '#dc3545');

            // ★★★ 音声監視を開始（タイマー機能） ★★★
            if (microphoneManager.stream) {
                startVoiceMonitoring(microphoneManager.stream);
            }
        } else {
            console.warn('⚠️ マイクの起動に失敗しました');
            updateMicStatus('エラー', '#dc3545');
        }

        // 分析を開始
        const result = await apiClient.startAnalysis();
        console.log('✅ 分析開始:', result);

    } catch (error) {
        console.error('❌ 起動エラー:', error);
        updateMicStatus('マイクエラー', '#dc3545');
    }
}

/**
 * マイクステータス表示を更新
 */
function updateMicStatus(text, color) {
    const micStatus = document.getElementById('micStatus');
    if (micStatus) {
        micStatus.textContent = `🎤 マイク: ${text}`;
        micStatus.style.color = color;
    }
}

/**
 * マイクイベントリスナーを設定
 */
function setupMicrophoneEvents() {
    window.addEventListener('microphone:recordingStarted', () => {
        updateMicStatus('録音中', '#dc3545');
        console.log('🎤 録音開始');
    });

    window.addEventListener('microphone:recordingStopped', () => {
        updateMicStatus('停止中', '#666');
        console.log('⏹️ 録音停止');
    });

    window.addEventListener('microphone:audioSent', (e) => {
        console.log('📤 音声データ送信完了:', e.detail);
    });

    window.addEventListener('microphone:recordingError', (e) => {
        console.error('❌ 録音エラー:', e.detail.error);
        updateMicStatus('エラー', '#dc3545');
    });

    window.addEventListener('microphone:audioSendError', (e) => {
        console.error('❌ 音声送信エラー:', e.detail.error);
    });

    window.addEventListener('microphone:audioConvertError', (e) => {
        console.error('❌ WAV変換エラー:', e.detail.error);
    });
}

/**
 * 面接停止処理
 */
async function stopInterview() {
    try {
        // 音声監視を停止
        stopVoiceMonitoring();

        // マイクを停止
        if (microphoneManager && microphoneManager.isRecording) {
            await microphoneManager.stop();
        }

        // カメラを停止
        if (cameraStream) {
            cameraStream.getTracks().forEach(track => track.stop());
            cameraStream = null;
        }

        // 分析を停止
        if (apiClient) {
            const result = await apiClient.stopAnalysis();
            console.log('分析結果:', result);
        }

        console.log('✅ 面接を正常に停止しました');

        // 結果ページへ遷移
        location.href = './interview-result';

    } catch (error) {
        console.error('❌ 停止処理エラー:', error);
        location.href = './interview-result';
    }
}

/**
 * クリーンアップ処理
 */
function cleanup() {
    try {
        // 音声監視を停止
        stopVoiceMonitoring();

        // カメラを停止
        if (cameraStream) {
            cameraStream.getTracks().forEach(track => {
                if (track.readyState !== 'ended') {
                    track.stop();
                }
            });
            cameraStream = null;
        }

        // マイクを停止
        if (microphoneManager && microphoneManager.isRecording) {
            microphoneManager.cleanup();
        }

        console.log('✅ クリーンアップ完了');
    } catch (error) {
        console.error('クリーンアップエラー:', error);
    }
}

// ========================================
// 質問管理
// ========================================

/**
 * 現在の質問を取得して表示
 */
function loadCurrentQuestion() {
    console.log('テスト: 質問読み込み開始');

    fetch('/interview/api/current-question')
        .then(response => {
            console.log('ステータス:', response.status);
            return response.json();
        })
        .then(data => {
            console.log('データ:', data);
            document.querySelector(".center-texts").textContent = data.question;
            document.querySelector(".progress").value = data.progress;
        })
        .catch(error => {
            console.error('エラー:', error);
            document.querySelector(".center-texts").textContent = '質問の読み込みに失敗しました';
        });
}

/**
 * 次の質問に進む
 */
function nextQuestion() {
    console.log('次の質問へ');

    // 無音タイマーをリセット
    if (silenceTimer) {
        clearTimeout(silenceTimer);
        silenceTimer = null;
    }

    isSpeaking = false;

    fetch('/interview/api/next-question', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            console.log('次の質問ステータス:', response.status);
            return response.json();
        })
        .then(data => {
            console.log('次の質問データ:', data);

            if (data.isFinished) {
                document.querySelector(".center-texts").textContent = '面接が終了しました。お疲れ様でした。';
                document.querySelector(".progress").value = 100;

                // ボタンを無効化
                const btn = document.querySelector('.button-next');
                if (btn) {
                    btn.disabled = true;
                    btn.textContent = '面接終了';
                }

                // 音声監視を停止
                stopVoiceMonitoring();
            } else {
                document.querySelector(".center-texts").textContent = data.question;
                document.querySelector(".progress").value = data.progress;
            }
        })
        .catch(error => {
            console.error('次の質問エラー:', error);
            document.querySelector(".center-texts").textContent = '質問の読み込みに失敗しました';
        });
}

// ========================================
// イベントリスナー
// ========================================

// ページ読み込み時の初期化
window.addEventListener('DOMContentLoaded', async () => {
    console.log('=== ページ初期化開始 ===');

    // マイクイベントを設定
    setupMicrophoneEvents();

    // カメラとマイクを起動
    await startCameraAndMicrophone();

    // 最初の質問を読み込む
    loadCurrentQuestion();

    // 次へボタンのイベントリスナーを設定
    const nextButton = document.querySelector('.button-next');
    if (nextButton) {
        nextButton.addEventListener('click', nextQuestion);
        console.log('✅ 次へボタンにイベント設定完了');
    }
});

// ページを離れる時のクリーンアップ
window.addEventListener('beforeunload', () => {
    cleanup();
});