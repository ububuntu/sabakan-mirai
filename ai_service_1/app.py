#面接評価システム - 軽量版Flaskバックエンド
#OpenCV基本機能 + 簡易姿勢チェック + 音声認識

from flask import Flask, request, jsonify
from flask_cors import CORS
import cv2
import numpy as np
import base64
from collections import defaultdict
import os
import traceback
import json

app = Flask(__name__)
CORS(app)

# ==================== 軽量版感情認識システム ====================
class LightEmotionRecognition:
    def __init__(self):
        try:
            self.face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_frontalface_default.xml')
            self.smile_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_smile.xml')
        except Exception as e:
            print(f"感情認識初期化エラー: {e}")
            raise
        
        self.emotion_counts = defaultdict(int)
        self.frame_count = 0
        self.is_running = False
    
    def analyze_frame(self, frame):
        """フレームから簡易的な感情を推定"""
        try:
            gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
            faces = self.face_cascade.detectMultiScale(gray, 1.3, 5)
            
            if len(faces) == 0:
                self.emotion_counts['neutral'] += 1
            else:
                for (x, y, w, h) in faces:
                    roi_gray = gray[y:y+h, x:x+w]
                    smiles = self.smile_cascade.detectMultiScale(roi_gray, 1.8, 20)
                    
                    if len(smiles) > 0:
                        self.emotion_counts['happy'] += 1
                    else:
                        self.emotion_counts['neutral'] += 1
                    break
            
            self.frame_count += 1
        except Exception as e:
            print(f"フレーム解析エラー: {e}")
    
    def get_score(self):
        """表情スコアを計算 (0-100)"""
        if self.frame_count == 0:
            return 70  # デフォルト値
        
        happy_rate = (self.emotion_counts.get('happy', 0) / self.frame_count) * 100
        # 笑顔率を点数に変換 (30%以上で満点、0%で60点)
        score = min(100, 60 + happy_rate)
        return int(score)
    
    def get_report(self):
        if self.frame_count == 0:
            return {'喜び': 0.0, '無表情': 100.0}
        
        happy_rate = (self.emotion_counts.get('happy', 0) / self.frame_count) * 100
        neutral_rate = (self.emotion_counts.get('neutral', 0) / self.frame_count) * 100
        
        return {
            '喜び': round(happy_rate, 1),
            '無表情': round(neutral_rate, 1)
        }
    
    def reset(self):
        self.emotion_counts = defaultdict(int)
        self.frame_count = 0
        self.is_running = False


# ==================== 軽量版姿勢チェックシステム ====================
class LightPostureCheck:
    def __init__(self):
        try:
            self.face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_frontalface_default.xml')
            self.eye_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_eye.xml')
        except Exception as e:
            print(f"姿勢チェック初期化エラー: {e}")
            raise
        
        self.stats = {
            'face_detected': 0,
            'face_centered': 0,
            'eyes_detected': 0,
            'total_frames': 0
        }
        self.is_running = False
    
    def check_posture(self, frame):
        try:
            gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
            faces = self.face_cascade.detectMultiScale(gray, 1.3, 5)
            
            h, w = frame.shape[:2]
            center_x = w // 2
            
            if len(faces) > 0:
                self.stats['face_detected'] += 1
                
                face = max(faces, key=lambda f: f[2] * f[3])
                x, y, fw, fh = face
                
                face_center_x = x + fw // 2
                
                if abs(face_center_x - center_x) < w * 0.2:
                    self.stats['face_centered'] += 1
                
                roi_gray = gray[y:y+fh, x:x+fw]
                eyes = self.eye_cascade.detectMultiScale(roi_gray, 1.1, 5)
                
                if len(eyes) >= 2:
                    self.stats['eyes_detected'] += 1
            
            self.stats['total_frames'] += 1
        except Exception as e:
            print(f"姿勢チェックエラー: {e}")
    
    def get_score(self):
        """姿勢スコアを計算 (0-100)"""
        total = self.stats['total_frames']
        if total == 0:
            return 70  # デフォルト値
        
        # 顔検出率、中心率、目検出率の平均
        face_rate = (self.stats['face_detected'] / total) * 100
        centered_rate = (self.stats['face_centered'] / total) * 100
        eyes_rate = (self.stats['eyes_detected'] / total) * 100
        
        score = (face_rate * 0.3 + centered_rate * 0.4 + eyes_rate * 0.3)
        return int(min(100, score))
    
    def get_report(self):
        total = self.stats['total_frames']
        
        if total == 0:
            return {
                'total_frames': 0,
                'perfect_rate': 0.0,
                'face_detection_rate': 0.0,
                'centered_rate': 0.0,
                'eyes_detection_rate': 0.0,
                'face_centered': {'success': 0, 'fail': 0},
                'face_straight': {'success': 0, 'fail': 0},
                'shoulders_level': {'success': 0, 'fail': 0}
            }
        
        # 完璧なフレームの割合を計算（3つの条件すべて満たす）
        perfect_count = 0
        for i in range(total):
            # 簡易的に: face_centered, eyes_detected, total_framesから推定
            pass
        
        return {
            'total_frames': total,
            'perfect_rate': round((self.stats['face_centered'] / total) * 100, 1),  # 簡易版
            'face_detection_rate': round((self.stats['face_detected'] / total) * 100, 1),
            'centered_rate': round((self.stats['face_centered'] / total) * 100, 1),
            'eyes_detection_rate': round((self.stats['eyes_detected'] / total) * 100, 1),
            # Kotlin Serviceが期待する詳細データ
            'face_centered': {
                'success': self.stats['face_centered'],
                'fail': total - self.stats['face_centered']
            },
            'face_straight': {
                'success': self.stats['eyes_detected'],  # 簡易的に目検出を代用
                'fail': total - self.stats['eyes_detected']
            },
            'shoulders_level': {
                'success': self.stats['face_centered'],  # 簡易的に中央配置を代用
                'fail': total - self.stats['face_centered']
            }
        }
    
    def reset(self):
        self.stats = {
            'face_detected': 0,
            'face_centered': 0,
            'eyes_detected': 0,
            'total_frames': 0
        }
        self.is_running = False


# ==================== 軽量版視線検出システム ====================
class LightGazeDetector:
    def __init__(self):
        try:
            self.face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_frontalface_default.xml')
            self.eye_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_eye.xml')
        except Exception as e:
            print(f"視線検出初期化エラー: {e}")
            raise
        
        self.gaze_counts = defaultdict(int)
        self.frame_count = 0
        self.eyes_closed_frames = 0
        self.is_running = False
    
    def process_frame(self, frame):
        try:
            gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
            faces = self.face_cascade.detectMultiScale(gray, 1.3, 5)
            
            if len(faces) == 0:
                self.gaze_counts['顔が検出されない'] += 1
            else:
                face = faces[0]
                x, y, w, h = face
                
                roi_gray = gray[y:y+h, x:x+w]
                eyes = self.eye_cascade.detectMultiScale(roi_gray, 1.1, 5)
                
                if len(eyes) == 0:
                    self.eyes_closed_frames += 1
                    self.gaze_counts['目が検出されない'] += 1
                elif len(eyes) >= 2:
                    self.eyes_closed_frames = 0
                    self.gaze_counts['正面を見ている'] += 1
                else:
                    self.gaze_counts['画面を見れていません'] += 1
            
            self.frame_count += 1
        except Exception as e:
            print(f"視線検出エラー: {e}")
    
    def get_score(self):
        """視線スコアを計算 (0-100)"""
        total = sum(self.gaze_counts.values())
        if total == 0:
            return 70  # デフォルト値
        
        # 正面を見ている割合をスコアに
        looking_front = self.gaze_counts.get('正面を見ている', 0)
        score = (looking_front / total) * 100
        return int(score)
    
    def get_report(self):
        total = sum(self.gaze_counts.values())
        report = {}
        
        for direction, count in self.gaze_counts.items():
            percentage = (count / total * 100) if total > 0 else 0.0
            report[direction] = {
                'count': count,
                'percentage': round(percentage, 1)
            }
        
        return {
            'total_frames': self.frame_count,
            'is_sleeping': self.eyes_closed_frames > 100,
            'directions': report
        }
    
    def reset(self):
        self.gaze_counts = defaultdict(int)
        self.frame_count = 0
        self.eyes_closed_frames = 0
        self.is_running = False


# ==================== 音声認識システム（簡易版）====================
class LightSpeechAnalyzer:
    def __init__(self):
        self.total_chars = 0
        self.total_duration = 0  # 秒
        self.transcripts = []
    
    def analyze_audio(self, text, duration=60):
        """音声テキストを解析（簡易版）"""
        char_count = len(text)
        self.total_chars += char_count
        self.total_duration += duration
        
        self.transcripts.append({
            'text': text,
            'chars': char_count,
            'duration': duration
        })
    
    def get_chars_per_minute(self):
        """1分あたりの文字数を計算"""
        if self.total_duration == 0:
            return 0
        
        minutes = self.total_duration / 60
        return int(self.total_chars / minutes)
    
    def get_score(self):
        """発話速度スコアを計算 (0-100)"""
        cpm = self.get_chars_per_minute()
        
        # 理想的な速度: 251-350文字/分
        if 251 <= cpm <= 350:
            return 100
        elif 201 <= cpm <= 250 or 351 <= cpm <= 400:
            return 80
        elif 150 <= cpm <= 200 or 401 <= cpm <= 450:
            return 70
        else:
            return 60
    
    def get_report(self):
        return {
            'total_chars': self.total_chars,
            'total_duration': self.total_duration,
            'chars_per_minute': self.get_chars_per_minute(),
            'transcripts': self.transcripts
        }
    
    def reset(self):
        self.total_chars = 0
        self.total_duration = 0
        self.transcripts = []


# ==================== グローバルインスタンス ====================
emotion_system = None
posture_system = None
gaze_system = None
speech_system = None

def init_systems():
    """システムを初期化"""
    global emotion_system, posture_system, gaze_system, speech_system

    try:
        if emotion_system is None:
            print("✨ EmotionRecognition 初期化中...")
            emotion_system = LightEmotionRecognition()
            print("✅ EmotionRecognition 初期化完了")

        if posture_system is None:
            print("✨ PostureCheck 初期化中...")
            posture_system = LightPostureCheck()
            print("✅ PostureCheck 初期化完了")

        if gaze_system is None:
            print("✨ GazeDetector 初期化中...")
            gaze_system = LightGazeDetector()
            print("✅ GazeDetector 初期化完了")
        
        if speech_system is None:
            print("✨ SpeechAnalyzer 初期化中...")
            speech_system = LightSpeechAnalyzer()
            print("✅ SpeechAnalyzer 初期化完了")
        
        return True
    except Exception as e:
        print(f"❌ システム初期化エラー: {e}")
        traceback.print_exc()
        return False


# ==================== APIエンドポイント ====================

@app.route('/')
def index():
    """トップページ（接続確認用）"""
    return jsonify({
        'status': 'ok',
        'message': 'Interview Analysis System is running',
        'version': '2.0.0'
    })


@app.route('/health')
def health():
    """ヘルスチェック"""
    return jsonify({
        'status': 'ok',
        'message': 'Server is running',
        'systems': {
            'emotion': emotion_system is not None,
            'posture': posture_system is not None,
            'gaze': gaze_system is not None,
            'speech': speech_system is not None
        }
    })


@app.route('/interview/start', methods=['POST'])
def start_analysis():
    """面接分析を開始 - Kotlin: startAnalysis()"""
    try:
        if not init_systems():
            return jsonify({
                'status': 'error',
                'message': 'システムの初期化に失敗しました'
            }), 500
        
        emotion_system.is_running = True
        posture_system.is_running = True
        gaze_system.is_running = True
        
        return jsonify({
            'status': 'success',
            'message': '面接分析を開始しました'
        })
    
    except Exception as e:
        traceback.print_exc()
        return jsonify({
            'status': 'error',
            'message': f'エラー: {str(e)}'
        }), 500


@app.route('/interview/analyze', methods=['POST'])
def analyze_frame():
    """フレーム画像を分析 - Kotlin: analyzeFrame(base64)"""
    try:
        if emotion_system is None:
            return jsonify({
                'status': 'error',
                'message': 'システムが初期化されていません。/interview/start を先に呼び出してください'
            }), 400
        
        data = request.get_json()
        if not data or 'image' not in data:
            return jsonify({
                'status': 'error',
                'message': 'imageフィールドが必要です'
            }), 400
        
        image_data = data['image']
        
        # Base64デコード
        try:
            if ',' in image_data:
                image_data = image_data.split(',')[1]
            image_bytes = base64.b64decode(image_data)
            nparr = np.frombuffer(image_bytes, np.uint8)
            frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
            
            if frame is None:
                return jsonify({
                    'status': 'error',
                    'message': '画像のデコードに失敗しました'
                }), 400
        except Exception as e:
            return jsonify({
                'status': 'error',
                'message': f'画像処理エラー: {str(e)}'
            }), 400
        
        # 解析実行
        if emotion_system.is_running:
            emotion_system.analyze_frame(frame)
        
        if posture_system.is_running:
            posture_system.check_posture(frame)
        
        if gaze_system.is_running:
            gaze_system.process_frame(frame)
        
        return jsonify({
            'status': 'success',
            'message': 'フレームを解析しました'
        })
    
    except Exception as e:
        traceback.print_exc()
        return jsonify({
            'status': 'error',
            'message': f'解析エラー: {str(e)}'
        }), 500


@app.route('/interview/analyze-audio', methods=['POST'])
def analyze_audio():
    """音声データを分析 - Kotlin: analyzeAudio(base64Audio)"""
    try:
        if speech_system is None:
            return jsonify({
                'status': 'error',
                'message': 'システムが初期化されていません'
            }), 400
        
        data = request.get_json()
        if not data or 'audio' not in data:
            return jsonify({
                'status': 'error',
                'message': 'audioフィールドが必要です'
            }), 400
        
        # Base64エンコードされた音声データまたはテキストを受け取る
        audio_data = data.get('audio', '')
        
        # テキストとして扱う（音声認識済みのテキスト）
        # または実際の音声認識を実装する場合はここで処理
        text = audio_data
        duration = data.get('duration', 60)  # デフォルト60秒
        
        speech_system.analyze_audio(text, duration)
        
        return jsonify({
            'status': 'success',
            'message': '音声を解析しました',
            'chars': len(text),
            'duration': duration
        })
    
    except Exception as e:
        traceback.print_exc()
        return jsonify({
            'status': 'error',
            'message': f'音声解析エラー: {str(e)}'
        }), 500


@app.route('/interview/audio', methods=['GET'])
def get_audio_result():
    """音声結果を取得 - Kotlin: getAudioResult()"""
    try:
        if speech_system is None:
            return jsonify({
                'status': 'error',
                'message': 'システムが初期化されていません'
            }), 400
        
        report = speech_system.get_report()
        
        # バイナリではなくJSONで返す（簡易版）
        return jsonify({
            'status': 'success',
            'data': report
        })
    
    except Exception as e:
        traceback.print_exc()
        return jsonify({
            'status': 'error',
            'message': f'エラー: {str(e)}'
        }), 500


@app.route('/interview/stop', methods=['POST'])
def stop_analysis():
    """面接分析を停止してレポートを返す - Kotlin: stopAnalysis()"""
    try:
        if emotion_system is None:
            return jsonify({
                'status': 'error',
                'message': 'システムが初期化されていません'
            }), 400
        
        emotion_system.is_running = False
        posture_system.is_running = False
        gaze_system.is_running = False
        
        # スコア計算（0-100点）
        expression_score = emotion_system.get_score()
        eyes_score = gaze_system.get_score()
        posture_score = posture_system.get_score()
        speech_speed_score = speech_system.get_score()
        
        # 詳細レポート取得
        emotion_report = emotion_system.get_report()
        posture_report = posture_system.get_report()
        gaze_report = gaze_system.get_report()
        speech_report = speech_system.get_report()
        
        # Kotlin Serviceが期待する形式で返す
        # calculateScores()メソッドがパースしやすい形式
        result = {
            'status': 'success',
            'message': '面接分析を停止しました',
            'report': {
                'emotion': emotion_report,
                'posture': posture_report,
                'gaze': gaze_report,
                'speech': speech_report
            },
            'scores': {
                'expression': expression_score,
                'eyes': eyes_score,
                'posture': posture_score,
                'speechSpeed': speech_speed_score
            }
        }
        
        return jsonify(result)
    
    except Exception as e:
        traceback.print_exc()
        return jsonify({
            'status': 'error',
            'message': f'停止エラー: {str(e)}'
        }), 500


@app.route('/interview/reset', methods=['POST'])
def reset_analysis():
    """面接分析データをリセット - Kotlin: reset()"""
    try:
        if emotion_system is None:
            return jsonify({
                'status': 'error',
                'message': 'システムが初期化されていません'
            }), 400
        
        emotion_system.reset()
        posture_system.reset()
        gaze_system.reset()
        speech_system.reset()
        
        return jsonify({
            'status': 'success',
            'message': 'データをリセットしました'
        })
    
    except Exception as e:
        traceback.print_exc()
        return jsonify({
            'status': 'error',
            'message': f'リセットエラー: {str(e)}'
        }), 500


if __name__ == '__main__':
    print("=" * 60)
    print("面接評価システム起動中（Kotlin対応版）...")
    print("=" * 60)
    print("\n利用可能なエンドポイント:")
    print("  GET  /              - 接続確認")
    print("  GET  /health        - ヘルスチェック")
    print("  POST /interview/start         - 分析開始")
    print("  POST /interview/analyze       - フレーム解析")
    print("  POST /interview/analyze-audio - 音声解析")
    print("  GET  /interview/audio         - 音声結果取得")
    print("  POST /interview/stop          - 分析停止＆レポート")
    print("  POST /interview/reset         - データリセット")
    print("=" * 60)
    
    app.run(debug=True, host='0.0.0.0', port=5000, threaded=True)