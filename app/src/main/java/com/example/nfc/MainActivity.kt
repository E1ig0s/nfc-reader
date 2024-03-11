package com.example.nfc

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NfcA
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private lateinit var nfcAdapter: NfcAdapter // NFC 어댑터
    private lateinit var textData: TextView // 텍스트 뷰
    private lateinit var pendingIntent: PendingIntent // 대기중인 인텐트
    private lateinit var intentFiltersArray: Array<IntentFilter> // 인텐트 필터 배열
    private lateinit var techListsArray: Array<Array<String>> // 기술 리스트 배열

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textData = findViewById(R.id.textData)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)?.also { adapter ->
            // NFC가 사용 불가능할 경우 사용자에게 알림
            if (adapter == null) {
                Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show()
                finish()
                return
            }
        }!!

        pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )

        // 필요한 NFC 기술 목록
        val techList = arrayOf(NfcA::class.java.name, Ndef::class.java.name)
        // NFC 태그가 검색됐을 때의 인텐트 필터
        intentFiltersArray = arrayOf(IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED))
        // 인텐트 필터 및 기술 목록 초기화
        techListsArray = arrayOf(techList)
    }

    override fun onResume() {
        super.onResume()
        // 액티비티가 사용자에게 보일 때 NFC 이벤트 처리를 위한 포그라운드 디스패치 설정
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray)
    }

    override fun onPause() {
        super.onPause()
        // 액티비티가 사용자에게 보이지 않을 때 포그라운드 디스패치 비활성화
        nfcAdapter.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // 새로운 인텐트가 있을 경우 NFC 태그 처리
        intent?.let { handleIntent(it) }
    }

    private fun handleIntent(intent: Intent) {
        // 인텐트 액션이 NFC 태그 발견과 일치할 경우
        if (NfcAdapter.ACTION_TECH_DISCOVERED == intent.action) {
            // 태그 정보를 추출하고 ndef 메시지를 읽어서 텍스트 뷰에 표시
            intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)?.let { tag ->
                val ndef = Ndef.get(tag)
                val messages = ndef.cachedNdefMessage?.records?.joinToString("\n") { String(it.payload) }
                textData.text = messages
            }
        }
    }
}
