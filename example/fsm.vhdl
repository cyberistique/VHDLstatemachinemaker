library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity fsm_new is
port(
    clk         : in std_logic;
    rst         : in std_logic;
   inp1   : in std_logic;
   out2   : out std_logic_vector(1 downto 0));
end entity fsm_new;

architecture beh of fsm_new is
type t_State is (S0,S1);
signal sstate : t_State;
begin
process(clk) is
begin
if rising_edge(clk) then
   if rst = '0' then
       sstate <= S0;
   else
case sstate is
when S0 =>
    if inp1 = '0' then
        sstate <= S0;
    else
        sstate <= S1;
    end if;
when S1 =>
    if inp1 = '0' then
        sstate <= S1;
    else
        sstate <= S0;
    end if;
end case;
   end if;
end if;
end process;

-- output decode
process(sstate) is
begin
case sstate is
when S0 =>
    out2 <= std_logic_vector(to_unsigned(0, 2));
when S1 =>
    out2 <= std_logic_vector(to_unsigned(2, 2));
end case;
end process;
end architecture;
