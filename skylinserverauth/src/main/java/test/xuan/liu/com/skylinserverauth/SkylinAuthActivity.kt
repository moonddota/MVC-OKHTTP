package test.xuan.liu.com.skylinserverauth

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import org.xutils.ex.DbException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.KeyManagementException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class SkylinAuthActivity : AppCompatActivity() {


    var reftifot: Retrofit? = null

    var iwork: Iwork? = null

    interface Iwork {
        fun saveLogintTable(logintable: LoginTable)
        fun saveUserId(userid: Int)
        fun requestFaiure(t: Throwable?)
        fun authSuccess(macList: List<ReqeustAuth>?)

    }


    fun initAuth(iwork: Iwork) {
        if (iwork == null) {
            Toast.makeText(this, "初始化失败:需要实现Iwork接口:", Toast.LENGTH_SHORT).show()
        } else {
            this.iwork = iwork
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.skylinauth_ly)

        intent = Intent("app.com.skylinservice.ui.forauth.ForAuthActivity")

        intent.putExtra("token", "token")
        startActivityForResult(intent, 100)

        val interceptor = HttpLoggingInterceptor()

        var client: OkHttpClient? = null
        try {

            val trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(null as KeyStore?)
            val trustManagers = trustManagerFactory.trustManagers
            if (trustManagers.size != 1 || trustManagers[0] !is X509TrustManager) {
                throw IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers))
            }
            val trustManager = trustManagers[0] as X509TrustManager

            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, arrayOf<TrustManager>(trustManager), null)
            val sslSocketFactory = sslContext.socketFactory

            val spec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2)
                    .cipherSuites(

                            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                            CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
                            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
                            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
                            CipherSuite.TLS_ECDHE_ECDSA_WITH_RC4_128_SHA,
                            CipherSuite.TLS_ECDHE_RSA_WITH_RC4_128_SHA,
                            CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA,
                            CipherSuite.TLS_DHE_DSS_WITH_AES_128_CBC_SHA,
                            CipherSuite.TLS_DHE_RSA_WITH_AES_256_GCM_SHA384,
                            CipherSuite.TLS_DH_anon_EXPORT_WITH_RC4_40_MD5,
                            CipherSuite.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256)
                    .build()


            client = OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .retryOnConnectionFailure(true)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .build()
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: KeyManagementException) {
            e.printStackTrace()
        }

        reftifot = Retrofit.Builder().baseUrl("http://devel.skylinuav.com/work/Public/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        val authModel = GsonUtils.changeGsonToBean(data.getStringExtra("itoken"), AuthModel::class.java)
        if (authModel.authed) {


            val service = reftifot?.create(HttpApiService::class.java)
            var call: Call<GeneralModel<AircrasftListModel<List<ReqeustAuth>>>>? = null
            try {
                call = service!!.getMacList(authModel.token, authModel.tid.toLong())
            } catch (e: DbException) {
                e.printStackTrace()
            }

            call!!.enqueue(object : Callback<GeneralModel<AircrasftListModel<List<ReqeustAuth>>>> {
                override fun onFailure(call: Call<GeneralModel<AircrasftListModel<List<ReqeustAuth>>>>?, t: Throwable?) {
                    iwork!!.requestFaiure(t)
                }

                override fun onResponse(call: Call<GeneralModel<AircrasftListModel<List<ReqeustAuth>>>>?, response: Response<GeneralModel<AircrasftListModel<List<ReqeustAuth>>>>?) {
                    iwork!!.saveUserId(authModel.userId)
                    val table = LoginTable()
                    table.username = ""
                    table.name = ""
                    table.password = ""
                    table.token = authModel.token
                    table.gauthority = 0
                    table.authority = 0
                    table.userId = authModel.userId
                    table.group = ""
                    table.isRememberLogin = true
                    iwork!!.saveLogintTable(table)
                    iwork!!.authSuccess(response?.body()?.data?.aircraft_list)
                }

            })



            this.iwork!!.saveUserId(authModel.userId)
            val table = LoginTable()
            table.username = ""
            table.name = ""
            table.password = ""
            table.token = authModel.token
            table.gauthority = 0
            table.authority = 0
            table.userId = authModel.userId
            table.group = ""
            table.isRememberLogin = true
            this.iwork!!.saveLogintTable(table)
//            Toast.makeText(this, "授权成功:", Toast.LENGTH_SHORT).show()

        } else {
//            Toast.makeText(this, "授权成功:", Toast.LENGTH_SHORT).show()
        }
    }
}
