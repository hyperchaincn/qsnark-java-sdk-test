package SdkTest;
import cn.qsnark.sdk.rpc.QsnarkAPI;
import cn.qsnark.sdk.rpc.function.FuncParamReal;
import cn.qsnark.sdk.rpc.function.FunctionDecode;
import cn.qsnark.sdk.rpc.returns.GetTokenReturn;
import cn.qsnark.sdk.rpc.returns.CompileReturn;
import cn.qsnark.sdk.rpc.returns.GetTxReciptReturn;
import org.testng.annotations.Test;
/*
 * Created by Hyperchain on 2017/9/8.
 */

public class SdkTest {

    // Token合约的ABI
    String TOKEN_ABI = "[{\"constant\":false,\"inputs\":[{\"name\":\"account\",\"type\":\"address\"},{\"name\":\"amount\",\"type\":\"uint256\"}],\"name\":" +
            "\"issue\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"to\",\"type\":\"address\"},{\"na" +
            "me\":\"amount\",\"type\":\"uint256\"}],\"name\":\"transfer\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"input" +
            "s\":[{\"name\":\"account\",\"type\":\"address\"}],\"name\":\"getBalance\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false," +
            "\"type\":\"function\"},{\"inputs\":[],\"payable\":false,\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"" +
            "account\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"amount\",\"type\":\"uint256\"}],\"name\":\"Issue\",\"type\":\"event\"},{\"anonymous\"" +
            ":false,\"inputs\":[{\"indexed\":false,\"name\":\"from\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"to\",\"type\":\"address\"},{\"indexed\":" +
            "false,\"name\":\"amount\",\"type\":\"uint256\"}],\"name\":\"Transfer\",\"type\":\"event\"}]";

    // Token合约的代码(去掉注视和换行'\n')
    String TOKEN_SOURCE = "pragma solidity ^0.4.10; contract Token {     address issuer;     mapping(address => uint) balances;          uint CODE_SUCCESS = 3300;     uint CODE_INSUFFICIENT_BALANCE = 3401;     uint CODE_INSUFFICIENT_PERMISSION = 3402;          function Token() {         issuer = msg.sender;     }     function issue(address account, uint amount) returns(uint) {         if (msg.sender != issuer) return CODE_INSUFFICIENT_PERMISSION;         balances[account] += amount;         return CODE_SUCCESS;     }     function transfer(address to, uint amount) returns(uint) {         if (balances[msg.sender] < amount) return CODE_INSUFFICIENT_BALANCE;         balances[msg.sender] -= amount;         balances[to] += amount;         return CODE_SUCCESS;     }     function getBalance(address account) constant returns(uint, uint) {         return(CODE_SUCCESS,balances[account]);     } }";

    // Token合约BIN
    String TOKEN_BIN = "0x6060604052341561000c57fe5b5b60008054600160a060020a03191633600160a060020a03161790555b5b6101ca806100396000396000f300606060405263ffffffff60e060020a600035041663867904b48114610037578063a9059cbb14610058578063f8b2cb4f14610079575bfe5b341561003f57fe5b610056600160a060020a03600435166024356100a7565b005b341561006057fe5b610056600160a060020a03600435166024356100e6565b005b341561008157fe5b610095600160a060020a036004351661017f565b60408051918252519081900360200190f35b60005433600160a060020a039081169116146100c35760006000fd5b600160a060020a03821660009081526001602052604090208054820190555b5050565b600160a060020a0333166000908152600160205260409020548190101561010d5760006000fd5b600160a060020a0333811660008181526001602090815260408083208054879003905593861680835291849020805486019055835192835282015280820183905290517fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef9181900360600190a15b5050565b600160a060020a0381166000908152600160205260409020545b9190505600a165627a7a72305820299e9bb6a492d60cb690d97c76ac26d821ff6bba1b863ce1b8720e449789692c0029";

    // 合约中定义的函数名
    String FUNC_ISSUE = "issue";
    String FUNC_TRANSFER = "transfer";
    String FUNC_GETBANLANCE = "getBalance";


    // 开发者平台授权相关
    private String clientId = "APPKEY";// 趣链开发者平台 AppKey
    private String clientSecert = "APPSECRET";// 趣链开发者平台 AppSecret
    private String phone = "PHONE";// 趣链开发者平台账号
    private String passwd = "PWD";// 趣链开发者平台密码

    // 区块链账户
    private String deployAccount = "DEPLOY_ACCOUNT";// 区块链账户（合约部署者）
    private String account1 = "ACCOUNT1";// 区块链账户1(资产持有人1)
    private String account2 = "ACCOUNT2";// 区块链账户2(资产持有人2)
    private String contractAddr = "CONTRACT_ADDR";// 部署后的合约地址
    private GetTokenReturn token;
    private QsnarkAPI api;


    public SdkTest() throws Exception {
        this.api = new QsnarkAPI();
        this.token = api.getAccess_Token(this.clientId,this.clientSecert, this.phone, this.passwd);
        System.out.println(this.token);
    }

    @Test
    public void TestCompile() throws Exception {
        CompileReturn compiled = this.api.compileContract(this.token.getAccess_token(), TOKEN_SOURCE);
        System.out.println("abi: " + compiled.getCts_abi());
        System.out.println("bin: " + compiled.getCts_bin());
    }

    @Test
    public void TestDeploySync() throws Exception {
        GetTxReciptReturn tx = this.api.deploysyncContract(
                this.token.getAccess_token(),
                TOKEN_BIN,
                deployAccount
        );
        System.out.println("contract_address: " + tx.getContract_address());
    }

    //通过invokesyncContract来调用合约中定义的issue函数
    @Test
    public void TestIssue() throws Exception {

        //为account1账户追加5000资产
        GetTxReciptReturn tx = this.api.invokesyncContract(
                this.token.getAccess_token(),
                false,
                deployAccount,//合约部署者发起起的调用申请
                contractAddr,
                TOKEN_ABI,
                FUNC_ISSUE,
                new FuncParamReal("address",account1),
                new FuncParamReal("uint",5000)
        );
    }

    //通过invokesyncContract来调用合约中定义的transfer函数
    @Test
    public void TestTransfer() throws Exception {

        //为account1账户追加5000资产
        GetTxReciptReturn tx = this.api.invokesyncContract(
                this.token.getAccess_token(),
                false,
                account1,//account1 发起起的转账申请
                contractAddr,
                TOKEN_ABI,
                FUNC_TRANSFER,
                new FuncParamReal("address",account2),
                new FuncParamReal("uint",2333)
        );
    }

    //通过invokesyncContract来调用合约中定义的getbalance函数
    @Test
    public void TestGetbalance() throws Exception {
        GetTxReciptReturn tx = this.api.invokesyncContract(
                this.token.getAccess_token(),
                false,
                account1,//account1发起的查询申请
                contractAddr,
                TOKEN_ABI,
                FUNC_GETBANLANCE,
                new FuncParamReal("address",account2)
        );
        //使用resultDecodeV2对tx.getRet()解码
        String result = FunctionDecode.resultDecodeV2(
                FUNC_GETBANLANCE,
                TOKEN_ABI,
                tx.getRet()
        );
        System.out.println("result : " + result );
    }


}