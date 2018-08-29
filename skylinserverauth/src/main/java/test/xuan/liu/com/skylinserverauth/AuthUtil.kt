package test.xuan.liu.com.skylinserverauth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import java.security.*
import java.security.cert.X509Certificate
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

/**
 * Created by liuxuan on 2018/3/5.
 */
class AuthUtil(var reftifot: Retrofit?, var cotext: Context, var iwork: Iwork?) {


    interface Iwork {
        fun saveLogintTable(logintable: LoginTable)
        fun saveTeam(teamid: Long, teamName:String)
        fun requestFaiure(t: Throwable?)
        fun authSuccess(macList: List<ReqeustAuth>?)
    }


    fun checkuOnactivityResult(data: Intent) {
        val authModel = GsonUtils.changeGsonToBean(data.getStringExtra("itoken"), AuthModel::class.java)
        if (authModel.authed) {
            iwork!!.saveTeam(authModel.tid.toLong(),authModel.teamName)
            val table = LoginTable()
            table.username = authModel.userName
            table.name = ""
            table.password = ""
            table.token = authModel.token
            table.gauthority = 0
            table.authority = 0
            table.userId = authModel.userId
            table.group = ""
            table.isRememberLogin = true
            iwork!!.saveLogintTable(table)
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
                    if (response?.body()?.ret == 200) {

                        iwork!!.authSuccess(response?.body()?.data?.aircraft_list)
                    } else
                        iwork!!.requestFaiure(Exception(response?.body()?.message))

                }

            })


        } else {
            iwork!!.requestFaiure(Exception("授权失败"))
        }
    }


    fun beginAuth() {

        if (cotext == null) {
            Toast.makeText(cotext, "上下文为空", Toast.LENGTH_SHORT).show()
            return
        }

        if (iwork == null) {
            Toast.makeText(cotext, "必须实现Iwork接口", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isAppInstallen(cotext, "app.com.skylinservice")) {
            iwork!!.requestFaiure(Exception("应用未安装,请先安装天麒服务"))
            return

        }

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.NONE else HttpLoggingInterceptor.Level.NONE

        var client: OkHttpClient? = null
        try {

            val xtm = object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}

                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}

                override fun getAcceptedIssuers(): Array<X509Certificate?> {
                    val x509Certificates = arrayOfNulls<X509Certificate>(0)
                    return x509Certificates
                }
            }

            var sslContext: SSLContext? = null
            try {
                sslContext = SSLContext.getInstance("SSL")

                sslContext!!.init(null, arrayOf<TrustManager>(xtm), SecureRandom())

            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            } catch (e: KeyManagementException) {
                e.printStackTrace()
            }

            val DO_NOT_VERIFY = object : HostnameVerifier {
                override fun verify(hostname: String, session: SSLSession): Boolean {
                    return true
                }
            }




            client = OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .retryOnConnectionFailure(true)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .sslSocketFactory(sslContext?.socketFactory)
                    .hostnameVerifier(DO_NOT_VERIFY)
                    .build()
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: KeyManagementException) {
            e.printStackTrace()
        }

        if (reftifot == null)
            reftifot = Retrofit.Builder().baseUrl("http://devel.skylinuav.com/work/Public/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build()

        var intent = Intent("app.com.skylinservice.ui.forauth.ForAuthActivity")

        intent.putExtra("token", "token")
        (cotext as Activity).startActivityForResult(intent, 100)

    }

    fun isAppInstallen(context: Context, packageName: String): Boolean {
        var pm = context.packageManager
        var installed: Boolean
        installed = try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            true
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            false
        }
        return installed
    }
}