// Instalar estos paquetes de NuGet antes de continuar
// https://docs.nuget.org/consume/nuget-faq
// Install-Package CodeContracts
// Install-Package FluentAssertions
//
// Agregar estas directivas a su código.
// using System;
// using CodeContracts;
// using FluentAssertions;
// using System.Security.Cryptography;
// using System.Globalization

void Main()
{
	string key = "0123456789ABCDEFFEDCBA9876543210";
	string pin = "1234";
	string pan = "7777770000075101538";
	
	PinBlockHelper pbHelper = new PinBlockHelper(key);
	string pinBlock = pbHelper.GetPinBlock(pan, pin);
	pinBlock.Should().Be("81C2C3AF6CA221A5");	
}

public class PinBlockHelper
{
	private ICryptoTransform encryptor;
	
	public PinBlockHelper(string key)
	{
		Requires.NotNullOrEmpty(key, nameof(key));
		Requires.True(key.Trim().Length == 32, nameof(key));
				
		this.encryptor = new TripleDESCryptoServiceProvider
		{
			KeySize = 128,
			Padding = PaddingMode.None,
			Mode = CipherMode.ECB,
			Key = key.Trim().ToByteArray()
		}.CreateEncryptor();
	}

	public string GetPinBlock(string pan, string pin)
	{
		Requires.NotNullOrEmpty(pan, nameof(pan));
		Requires.NotNullOrEmpty(pin, nameof(pin));
		Requires.LengthGreaterOrEqual(pan, 12, nameof(pan));
		Requires.LengthGreaterOrEqual(pin, 4, nameof(pin));
		
		string formattedPin = string.Concat("0", pin.Length, pin).PadRight(16, 'F');
		string formattedPan = pan.Substring(0, pan.Length - 1);
		formattedPan = formattedPan.Substring(formattedPan.Length - 12).PadLeft(16, '0');
		byte[] xor = formattedPin.Xor(formattedPan);		
		return this.encryptor.TransformFinalBlock(xor, 0, xor.Length).FromByteArray();
	}
}

public static class StringExtensions
{
	public static string FromByteArray(this byte[] input)
	{
		if (input == null)
		{
			return default(string);
		}
		
		return string.Join(string.Empty, input.Select(item => item.ToString("X2")));
	}
	
	public static byte[] ToByteArray(this string text)
	{
		Requires.NotNullOrEmpty(text, nameof(text));
		
		if (text.Length % 2 == 1)
		{
			text = "0" + text;
		}

		byte[] result = new byte[text.Length/2];
		for (int index = 0; index < text.Length; index += 2)
		{
			result[index/2] = byte.Parse(text.Substring(index, 2), NumberStyles.HexNumber);
		}

		return result;
	}

	public static byte[] Xor(this string operator1, string operator2)
	{
		Requires.NotNullOrEmpty(operator1, nameof(operator1));
		Requires.NotNullOrEmpty(operator2, nameof(operator2));
		Requires.True(operator1.Length == operator2.Length, "operator1.Length must be equals than operator2.Length");
		
		byte[] op1 = operator1.ToByteArray();
		byte[] op2 = operator2.ToByteArray();
		
		byte[] result = new byte[op1.Length];
		for (int index = 0; index < op1.Length; index++)
		{
			result[index] = Convert.ToByte(op1[index] ^ op2[index]);
		}
		
		return result;
	}	
}
